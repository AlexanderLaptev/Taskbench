#!/bin/bash

BASE_DIR=$(dirname "$0")
TEMPLATE_WORKER_SCRIPT="${BASE_DIR}/lib/backup_worker.sh.template"
INSTALLED_WORKER_DIR="${HOME}/.pg_backup_manager/scripts"
CONFIG_FILE="${HOME}/.pg_backup_manager/config"
CRON_JOB_COMMENT_PREFIX="PG_AUTO_BACKUP_JOB_FOR"

ask_value() {
    local prompt="$1"
    local default_value="$2"
    local var_name="$3"
    local input_value
    read -r -p "$prompt [$default_value]: " input_value
    eval "$var_name=\"${input_value:-$default_value}\""
}

ask_password() {
    local prompt="$1"
    local var_name="$2"
    local pass
    echo -n "$prompt: "
    stty -echo
    read -r pass
    stty echo
    echo
    eval "$var_name=\"$pass\""
}

echo "--- Установка автоматического бэкапа PostgreSQL ---"

ask_value "Имя базы данных для бэкапа" "" DB_NAME
while [ -z "$DB_NAME" ]; do echo "Имя БД не может быть пустым."; ask_value "Имя базы данных" "" DB_NAME; done

ask_value "Пользователь PostgreSQL" "$(whoami)" DB_USER
ask_value "Хост PostgreSQL (оставьте пустым для localhost)" "localhost" DB_HOST
ask_value "Порт PostgreSQL (оставьте пустым для 5432)" "5432" DB_PORT
ask_password "Пароль для пользователя ${DB_USER}" DB_PASSWORD
while [ -z "$DB_PASSWORD" ]; do echo "Пароль не может быть пустым."; ask_password "Пароль для пользователя ${DB_USER}" DB_PASSWORD; done

DEFAULT_BACKUP_DIR="${HOME}/pg_backups/${DB_NAME}"
ask_value "Директория для хранения бэкапов" "$DEFAULT_BACKUP_DIR" BACKUP_DIR
ask_value "Количество дней хранения бэкапов (целое число)" "7" DAYS_TO_KEEP
while ! [[ "$DAYS_TO_KEEP" =~ ^[0-9]+$ ]] || [ "$DAYS_TO_KEEP" -lt 1 ]; do
    echo "Некорректное значение. Введите целое положительное число."
    ask_value "Количество дней хранения бэкапов" "7" DAYS_TO_KEEP
done

ask_value "Минута для запуска cron (0-59)" "30" CRON_MINUTE
while ! [[ "$CRON_MINUTE" =~ ^[0-5]?[0-9]$ ]]; do echo "Неверное значение для минут."; ask_value "Минута (0-59)" "30" CRON_MINUTE; done
ask_value "Час для запуска cron (0-23)" "2" CRON_HOUR
while ! [[ "$CRON_HOUR" =~ ^([0-1]?[0-9]|2[0-3])$ ]]; do echo "Неверное значение для часов."; ask_value "Час (0-23)" "2" CRON_HOUR; done

mkdir -p "$INSTALLED_WORKER_DIR"
mkdir -p "$BACKUP_DIR"
mkdir -p "$(dirname "$CONFIG_FILE")"

PGPASS_FILE="${HOME}/.pgpass"
EFFECTIVE_DB_HOST=${DB_HOST:-localhost}
EFFECTIVE_DB_PORT=${DB_PORT:-5432}
PGPASS_ENTRY="${EFFECTIVE_DB_HOST}:${EFFECTIVE_DB_PORT}:${DB_NAME}:${DB_USER}:${DB_PASSWORD}"
PGPASS_ENTRY_CHECK="${EFFECTIVE_DB_HOST}:${EFFECTIVE_DB_PORT}:${DB_NAME}:${DB_USER}:" # Для проверки без пароля

echo "Настройка $PGPASS_FILE..."
if [ -f "$PGPASS_FILE" ]; then
    if grep -qF "$PGPASS_ENTRY_CHECK" "$PGPASS_FILE"; then
        echo "Похожая запись уже существует в $PGPASS_FILE."
        read -r -p "Хотите обновить её с новым паролем? (y/N): " update_pgpass
        if [[ "$update_pgpass" =~ ^[Yy]$ ]]; then
            grep -vF "$PGPASS_ENTRY_CHECK" "$PGPASS_FILE" > "${PGPASS_FILE}.tmp" && mv "${PGPASS_FILE}.tmp" "$PGPASS_FILE"
            echo "$PGPASS_ENTRY" >> "$PGPASS_FILE"
            echo "Запись в $PGPASS_FILE обновлена."
        else
            echo "Запись в $PGPASS_FILE не изменена. Убедитесь, что существующий пароль корректен."
        fi
    else
        echo "$PGPASS_ENTRY" >> "$PGPASS_FILE"
        echo "Запись добавлена в $PGPASS_FILE."
    fi
else
    echo "$PGPASS_ENTRY" > "$PGPASS_FILE"
    echo "$PGPASS_FILE создан с новой записью."
fi
chmod 600 "$PGPASS_FILE"
echo "Установлены права 600 на $PGPASS_FILE."

SAFE_DB_NAME_FOR_SCRIPT=$(echo "${DB_NAME}" | tr -cd '[:alnum:]_-')
SAFE_HOST_FOR_SCRIPT=$(echo "${EFFECTIVE_DB_HOST}" | tr -cd '[:alnum:]_-')
INSTALLED_WORKER_SCRIPT_NAME="backup_worker_${SAFE_DB_NAME_FOR_SCRIPT}_on_${SAFE_HOST_FOR_SCRIPT}.sh"
INSTALLED_WORKER_SCRIPT_PATH="${INSTALLED_WORKER_DIR}/${INSTALLED_WORKER_SCRIPT_NAME}"

echo "Генерация скрипта бэкапа: $INSTALLED_WORKER_SCRIPT_PATH"
sed -e "s|__DB_NAME__|${DB_NAME}|g" \
    -e "s|__DB_USER__|${DB_USER}|g" \
    -e "s|__DB_HOST__|${EFFECTIVE_DB_HOST}|g" \
    -e "s|__DB_PORT__|${EFFECTIVE_DB_PORT}|g" \
    -e "s|__BACKUP_DIR__|${BACKUP_DIR}|g" \
    -e "s|__DAYS_TO_KEEP__|${DAYS_TO_KEEP}|g" \
    "$TEMPLATE_WORKER_SCRIPT" > "$INSTALLED_WORKER_SCRIPT_PATH"

if [ $? -ne 0 ]; then
    echo "ОШИБКА: Не удалось создать скрипт $INSTALLED_WORKER_SCRIPT_PATH"
    exit 1
fi
chmod +x "$INSTALLED_WORKER_SCRIPT_PATH"
echo "Скрипт бэкапа создан и сделан исполняемым."

CRON_JOB_COMMENT="${CRON_JOB_COMMENT_PREFIX}_${SAFE_DB_NAME_FOR_SCRIPT}_ON_${SAFE_HOST_FOR_SCRIPT}"
CRON_JOB_LINE="${CRON_MINUTE} ${CRON_HOUR} * * * /bin/bash ${INSTALLED_WORKER_SCRIPT_PATH} ${CRON_JOB_COMMENT}"

echo "Добавление задачи в crontab..."
(crontab -l 2>/dev/null | grep -vF "$CRON_JOB_COMMENT" ; echo "$CRON_JOB_LINE") | crontab -
if [ $? -eq 0 ]; then
    echo "Задача успешно добавлена/обновлена в crontab."
    echo "Бэкап будет выполняться ежедневно в ${CRON_HOUR}:${CRON_MINUTE}."
else
    echo "ОШИБКА при добавлении задачи в crontab. Попробуйте добавить вручную:"
    echo "$CRON_JOB_LINE"
fi

CONFIG_KEY_PREFIX="${SAFE_DB_NAME_FOR_SCRIPT}_ON_${SAFE_HOST_FOR_SCRIPT}"
{
    echo "${CONFIG_KEY_PREFIX}_WORKER_SCRIPT_PATH=\"${INSTALLED_WORKER_SCRIPT_PATH}\""
    echo "${CONFIG_KEY_PREFIX}_BACKUP_DIR=\"${BACKUP_DIR}\""
    echo "${CONFIG_KEY_PREFIX}_CRON_COMMENT=\"${CRON_JOB_COMMENT}\""
    echo "${CONFIG_KEY_PREFIX}_DB_NAME=\"${DB_NAME}\""
    echo "${CONFIG_KEY_PREFIX}_DB_USER=\"${DB_USER}\""
    echo "${CONFIG_KEY_PREFIX}_DB_HOST=\"${EFFECTIVE_DB_HOST}\""
    echo "${CONFIG_KEY_PREFIX}_DB_PORT=\"${EFFECTIVE_DB_PORT}\""
} >> "$CONFIG_FILE"
chmod 600 "$CONFIG_FILE"

echo "--- Установка завершена! ---"
echo "Проверьте работу, запустив: ${INSTALLED_WORKER_SCRIPT_PATH}"
echo "Логи будут в ${BACKUP_DIR}/_backup.log"