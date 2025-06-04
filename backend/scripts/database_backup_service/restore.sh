#!/bin/bash

CONFIG_FILE="${HOME}/.pg_backup_manager/config"

ask_value() {
    local prompt="$1"
    local default_value="$2"
    local var_name="$3"
    local input_value
    read -r -p "$prompt [$default_value]: " input_value
    eval "$var_name=\"${input_value:-$default_value}\""
}

echo "--- Восстановление бэкапа PostgreSQL ---"

BACKUP_DIR_TO_RESTORE=""
DEFAULT_DB_NAME=""
DEFAULT_DB_USER=""
DEFAULT_DB_HOST="localhost"
DEFAULT_DB_PORT="5432"

if [ -f "$CONFIG_FILE" ]; then
    echo "Найдены следующие настроенные конфигурации бэкапов:"

    declare -A backup_configs
    config_idx=1
    while IFS= read -r line; do
        if [[ "$line" =~ ^([^_]+_ON_[^_]+)_DB_NAME=\"(.*)\"$ ]]; then
            prefix="${BASH_REMATCH[1]}"
            db_name_display="${BASH_REMATCH[2]}"
            backup_dir_display=$(grep "^${prefix}_BACKUP_DIR=" "$CONFIG_FILE" | cut -d'"' -f2)
            if [ -n "$backup_dir_display" ]; then
                 echo "$config_idx) БД: $db_name_display (Бэкапы в: $backup_dir_display)"
                 backup_configs[$config_idx]="$backup_dir_display;$db_name_display"
                 ((config_idx++))
            fi
        fi
    done < <(grep "_DB_NAME=" "$CONFIG_FILE" | sort -u)


    echo "$config_idx) Ввести путь к директории бэкапов вручную"
    echo "0) Отмена"

    read -r -p "Выберите конфигурацию для восстановления или введите путь вручную (0 для отмены): " choice
    if ! [[ "$choice" =~ ^[0-9]+$ ]]; then echo "Неверный ввод."; exit 1; fi
    if [ "$choice" -eq 0 ]; then echo "Отмена."; exit 0; fi

    if [ "$choice" -lt "$config_idx" ]; then
        selected_config=${backup_configs[$choice]}
        BACKUP_DIR_TO_RESTORE=$(echo "$selected_config" | cut -d';' -f1)
        DEFAULT_DB_NAME=$(echo "$selected_config" | cut -d';' -f2)
    fi
fi

if [ -z "$BACKUP_DIR_TO_RESTORE" ]; then
    ask_value "Введите полный путь к директории с бэкапами" "" BACKUP_DIR_TO_RESTORE
fi

if [ ! -d "$BACKUP_DIR_TO_RESTORE" ]; then
    echo "ОШИБКА: Директория $BACKUP_DIR_TO_RESTORE не найдена."
    exit 1
fi

echo -e "\nДоступные файлы бэкапов в $BACKUP_DIR_TO_RESTORE (формат .dump.gz):"
mapfile -t backup_files < <(find "$BACKUP_DIR_TO_RESTORE" -maxdepth 1 -type f -name "*.dump.gz" -printf "%T@ %p\n" | sort -nr | cut -d' ' -f2- | xargs -L1 basename)

if [ ${#backup_files[@]} -eq 0 ]; then
    echo "Бэкапы не найдены в $BACKUP_DIR_TO_RESTORE."
    exit 1
fi

for i in "${!backup_files[@]}"; do
    printf "%3d) %s\n" $((i+1)) "${backup_files[$i]}"
done
echo "  0) Отмена"

read -r -p "Введите номер файла для восстановления: " file_choice
if ! [[ "$file_choice" =~ ^[0-9]+$ ]] || [ "$file_choice" -lt 0 ] || [ "$file_choice" -gt ${#backup_files[@]} ]; then
    echo "Неверный выбор."
    exit 1
fi
if [ "$file_choice" -eq 0 ]; then echo "Восстановление отменено."; exit 0; fi


SELECTED_BACKUP_GZ_BASENAME="${backup_files[$((file_choice-1))]}"
SELECTED_BACKUP_GZ_FULLPATH="${BACKUP_DIR_TO_RESTORE}/${SELECTED_BACKUP_GZ_BASENAME}"
SELECTED_BACKUP_DUMP_FULLPATH="${SELECTED_BACKUP_GZ_FULLPATH%.gz}" # Убираем .gz

echo "Выбран файл: $SELECTED_BACKUP_GZ_FULLPATH"

echo -e "\nВведите данные для подключения к ЦЕЛЕВОЙ базе данных PostgreSQL (КУДА восстанавливать):"
ask_value "Имя целевой базы данных" "$DEFAULT_DB_NAME" RESTORE_DB_NAME
while [ -z "$RESTORE_DB_NAME" ]; do echo "Имя БД не может быть пустым."; ask_value "Имя целевой базы данных" "$DEFAULT_DB_NAME" RESTORE_DB_NAME; done

ask_value "Пользователь PostgreSQL для восстановления" "$(whoami)" RESTORE_DB_USER
ask_value "Хост PostgreSQL (оставьте пустым для localhost)" "$DEFAULT_DB_HOST" RESTORE_DB_HOST
ask_value "Порт PostgreSQL (оставьте пустым для 5432)" "$DEFAULT_DB_PORT" RESTORE_DB_PORT

CLEAN_BEFORE_RESTORE="n"
read -r -p "Очистить целевую базу данных перед восстановлением (DROP OBJECTS)? (y/N): " confirm_clean
if [[ "$confirm_clean" =~ ^[Yy]$ ]]; then
    CLEAN_BEFORE_RESTORE="y"
fi

echo -e "\nВНИМАНИЕ: Это действие перезапишет данные в базе '${RESTORE_DB_NAME}' на '${RESTORE_DB_HOST:-localhost}'!"
if [[ "$CLEAN_BEFORE_RESTORE" == "y" ]]; then
    echo "Все существующие объекты в базе '${RESTORE_DB_NAME}' будут УДАЛЕНЫ перед восстановлением."
fi
read -r -p "Вы абсолютно уверены, что хотите продолжить? (Введите 'YES_RESTORE' для подтверждения): " confirm_action
if [ "$confirm_action" != "YES_RESTORE" ]; then
    echo "Восстановление отменено."
    exit 0
fi

echo "Распаковка $SELECTED_BACKUP_GZ_FULLPATH..."
gunzip -k -f "$SELECTED_BACKUP_GZ_FULLPATH" # -k оставляет .gz, -f перезаписывает .dump если есть
if [ $? -ne 0 ]; then echo "ОШИБКА: не удалось распаковать $SELECTED_BACKUP_GZ_FULLPATH"; exit 1; fi
echo "Файл распакован: $SELECTED_BACKUP_DUMP_FULLPATH"

RESTORE_OPTS="-d $RESTORE_DB_NAME -U $RESTORE_DB_USER -v" # -v для подробного вывода
EFFECTIVE_RESTORE_DB_HOST=${RESTORE_DB_HOST:-localhost}
EFFECTIVE_RESTORE_DB_PORT=${RESTORE_DB_PORT:-5432}

if [ "$EFFECTIVE_RESTORE_DB_HOST" != "localhost" ] && [ "$EFFECTIVE_RESTORE_DB_HOST" != "127.0.0.1" ]; then
    RESTORE_OPTS="$RESTORE_OPTS -h $EFFECTIVE_RESTORE_DB_HOST"
fi
if [ "$EFFECTIVE_RESTORE_DB_PORT" != "5432" ]; then
    RESTORE_OPTS="$RESTORE_OPTS -p $EFFECTIVE_RESTORE_DB_PORT"
fi

if [[ "$CLEAN_BEFORE_RESTORE" == "y" ]]; then
    RESTORE_OPTS="$RESTORE_OPTS --clean"
fi

echo "Запуск pg_restore..."
pg_restore $RESTORE_OPTS "$SELECTED_BACKUP_DUMP_FULLPATH"

if [ $? -eq 0 ]; then
    echo "Восстановление успешно завершено."
else
    echo "ОШИБКА во время восстановления. Проверьте вывод выше."
fi

read -r -p "Удалить распакованный файл ${SELECTED_BACKUP_DUMP_FULLPATH}? (y/N): " delete_dump
if [[ "$delete_dump" =~ ^[Yy]$ ]]; then
    rm -f "$SELECTED_BACKUP_DUMP_FULLPATH"
    echo "Файл $SELECTED_BACKUP_DUMP_FULLPATH удален."
fi

echo "--- Процесс восстановления завершен ---"