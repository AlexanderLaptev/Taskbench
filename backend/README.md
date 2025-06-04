# Инструкция по развертыванию

1. Создать .env файл, в котором указать переменные среды:
   - Используются для сборки
    > DJANGO_SECRET_KEY - используется для хеширования

    > DATABASE_NAME - название базы данных

    > DATABASE_USERNAME - пользователь базы данных

    > DATABASE_PASSWORD - пароль от базы данных

    > GIGACHAT_AUTH_KEY - API ключ от Gigachat
    
    > YOOKASSA_AUTH_KEY - API ключ от ЮKassa
    
    > YOOKASSA_STORE_ID - идентификатор от магазина в ЮKassa
   
    > SERVER_HOST - ip сервера

   - Используются в github actions
    > SERVER_USER - ssh пользователь

    > SERVER_PASSWORD - пароль от ssh пользователя

    > DOCKER_USERNAME - пользователь dockerhub с docker image

    > DOCKER_PASSWORD - пароль от dockerhub

    > DOCKER_TOKEN - токен аккаунта dockerhub
2. Для локального тестирования можно поднять контейнеры локально:
```
   docker compose up --build
```
В этом случае используется docker-compose.override.yaml, в котором указана сборка DOCKERFILE

3. Для развертывания на сервере используется:
 - на github runner-е
```
   docker build -t ${{ DOCKER_USERNAME }}/taskbench-backend:latest .
   docker push ${{ DOCKER_USERNAME }}/taskbench-backend:latest
```
- на сервере
```
   docker compose pull
   docker compose down
   docker compose up -d
```
4. Для получения записей логов:
```
   docker logs taskbench-backend
```
или 
```
   docker logs taskbench-backend -f
```

# Создание резервных копий базы данных
Версия postgresql: 17

В директории [/scripts/database_backup_service](https://github.com/AlexanderLaptev/Taskbench/tree/backend-django/backend/scripts/database_backup_service) находится bash-скрипт, добавляющий задачу создания резервных копий в crontab. 

`install.sh` - установка задачи 
`restore.sh` - восстановление резервной копии
`uninstall.sh` - удаление задачи, очистка сохраненных копий.
