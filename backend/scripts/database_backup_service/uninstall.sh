#!/bin/bash

CONFIG_FILE="${HOME}/.pg_backup_manager/config"

echo "--- Удаление автоматического бэкапа PostgreSQL ---"

if [ ! -f "$CONFIG_FILE" ]; then
    echo "Файл конфигурации $CONFIG_FILE не найден. Удаление невозможно."
    exit 1
fi

echo "Найдены следующие настроенные бэкапы:"
grep -oP '^[^_]+_ON_[^_]+' "$CONFIG_FILE" | sort -u | nl
echo "0) Отмена"

read -r -p "Введите номер бэкапа для удаления (или 0 для отмены): " choice
if ! [[ "$choice" =~ ^[0-9]+$ ]]; then
    echo "Неверный ввод."
    exit 1
fi
if [ "$choice" -eq 0 ]; then
    echo "Удаление отменено."
    exit 0
fi

SELECTED_PREFIX=$(grep -oP '^[^_]+_ON_[^_]+' "$CONFIG_FILE" | sort -u | sed -n "${choice}p")

if [ -z "$SELECTED_PREFIX" ]; then
    echo "Неверный выбор."
    exit 1
fi

echo "Выбран для удаления бэкап с идентификатором: $SELECTED_PREFIX"

WORKER_SCRIPT_PATH=$(grep "^${SELECTED_PREFIX}_WORKER_SCRIPT_PATH=" "$CONFIG_FILE" | cut -d'"' -f2)
BACKUP_DIR=$(grep "^${SELECTED_PREFIX}_BACKUP_DIR=" "$CONFIG_FILE" | cut -d'"' -f2)
CRON_COMMENT=$(grep "^${SELECTED_PREFIX}_CRON_COMMENT=" "$CONFIG_FILE" | cut -d'"' -f2)
DB_NAME_FOR_PGPASS=$(grep "^${SELECTED_PREFIX}_DB_NAME=" "$CONFIG_FILE" | cut -d'"' -f2)
DB_USER_FOR_PGPASS=$(grep "^${SELECTED_PREFIX}_DB_USER=" "$CONFIG_FILE" | cut -d'"' -f2)
DB_HOST_FOR_PGPASS=$(grep "^${SELECTED_PREFIX}_DB_HOST=" "$CONFIG_FILE" | cut -d'"' -f2)
DB_PORT_FOR_PGPASS=$(grep "^${SELECTED_PREFIX}_DB_PORT=" "$CONFIG_FILE" | cut -d'"' -f2)


read -r -p "Вы уверены, что хотите удалить конфигурацию бэкапа для '${DB_NAME_FOR_PGPASS}' на '${DB_HOST_FOR_PGPASS}'? (y/N): " confirm_delete
if [[ ! "$confirm_delete" =~ ^[Yy]$ ]]; then
    echo "Удаление отменено."
    exit 0
fi

if [ -n "$CRON_COMMENT" ]; then
    echo "Удаление задачи из crontab с комментарием: $CRON_COMMENT"
    (crontab -l 2>/dev/null | grep -vF "$CRON_COMMENT") | crontab -
    if [ $? -eq 0 ]; then echo "Задача из crontab удалена (если существовала)."; else echo "Ошибка при удалении из crontab."; fi
else
    echo "Комментарий для cron не найден в конфигурации."
fi

if [ -f "$WORKER_SCRIPT_PATH" ]; then
    read -r -p "Удалить скрипт бэкапа ${WORKER_SCRIPT_PATH}? (y/N): " delete_script
    if [[ "$delete_script" =~ ^[Yy]$ ]]; then
        rm -f "$WORKER_SCRIPT_PATH"
        echo "Скрипт $WORKER_SCRIPT_PATH удален."
    fi
else
    echo "Скрипт воркера $WORKER_SCRIPT_PATH не найден."
fi

if [ -d "$BACKUP_DIR" ]; then
    read -r -p "УДАЛИТЬ ВСЕ БЭКАПЫ в директории ${BACKUP_DIR}? (ОЧЕНЬ ОПАСНО!) Введите 'YES_DELETE_BACKUPS' для подтверждения: " confirm_del_backups
    if [ "$confirm_del_backups" == "YES_DELETE_BACKUPS" ]; then
        rm -rf "$BACKUP_DIR"
        echo "Директория $BACKUP_DIR со всеми бэкапами удалена."
    else
        echo "Директория бэкапов $BACKUP_DIR НЕ удалена."
    fi
fi

echo "Удаление записей из $CONFIG_FILE..."
grep -v "^${SELECTED_PREFIX}_" "$CONFIG_FILE" > "${CONFIG_FILE}.tmp" && mv "${CONFIG_FILE}.tmp" "$CONFIG_FILE"
echo "Записи для $SELECTED_PREFIX удалены из файла конфигурации."
if [ ! -s "$CONFIG_FILE" ]; then
    rm -f "$CONFIG_FILE"
    echo "Файл конфигурации $CONFIG_FILE был пуст и удален."
    if [ -d "$(dirname "$WORKER_SCRIPT_PATH")" ] && [ -z "$(ls -A "$(dirname "$WORKER_SCRIPT_PATH")")" ]; then
        rmdir "$(dirname "$WORKER_SCRIPT_PATH")"
        echo "Директория $(dirname "$WORKER_SCRIPT_PATH") удалена, так как пуста."
    fi
    if [ -d "$(dirname "$CONFIG_FILE")" ] && [ -z "$(ls -A "$(dirname "$CONFIG_FILE")")" ]; then
        rmdir "$(dirname "$CONFIG_FILE")"
        echo "Директория $(dirname "$CONFIG_FILE") удалена, так как пуста."
    fi
fi

echo -e "\nНАПОМИНАНИЕ: Если вы хотите удалить соответствующую запись из ~/.pgpass, сделайте это вручную."
echo "Запись для удаления могла выглядеть так: ${DB_HOST_FOR_PGPASS}:${DB_PORT_FOR_PGPASS}:${DB_NAME_FOR_PGPASS}:${DB_USER_FOR_PGPASS}:ВАШ_ПАРОЛЬ"

echo "--- Удаление завершено ---"