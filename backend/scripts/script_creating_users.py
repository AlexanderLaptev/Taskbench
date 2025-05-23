from datetime import datetime, timedelta

import requests

BASE_URL = "http://127.0.0.1:8000"
USERS = [
    {"email": "user1@example.com", "password": "password1"},
    {"email": "user2@example.com", "password": "password2"},
    {"email": "user3@example.com", "password": "password3"}
]


def register_user(email, password):
    url = f"{BASE_URL}/user/register/"
    data = {
        "email": email,
        "password": password
    }
    response = requests.post(url, json=data)
    return response.json()


def login_user(email, password):
    url = f"{BASE_URL}/user/login/"
    data = {
        "email": email,
        "password": password
    }
    response = requests.post(url, json=data)
    return response.json()


def create_category(token, name):
    url = f"{BASE_URL}/categories/"
    headers = {"Authorization": f"Bearer {token}"}
    data = {"name": name}
    response = requests.post(url, headers=headers, json=data)
    return response.json()


def create_task(token, content, deadline, priority, category_id=None):
    url = f"{BASE_URL}/tasks/"
    headers = {"Authorization": f"Bearer {token}"}

    dpc = {
        "deadline": deadline,
        "priority": priority
    }
    if category_id:
        dpc["category_id"] = category_id

    data = {
        "content": content,
        "dpc": dpc,
        "subtasks": [
            {"content": f"Подзадача 1 для {content}"},
            {"content": f"Подзадача 2 для {content}"}
        ]
    }
    response = requests.post(url, headers=headers, json=data)
    return response.json()


def get_tasks(token, params=None):
    url = f"{BASE_URL}/tasks/"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers, params=params)
    return response.json()


def create_subtask(token, task_id, content):
    url = f"{BASE_URL}/subtasks/?task_id={task_id}"
    headers = {"Authorization": f"Bearer {token}"}
    data = {
        "content": content,
        "is_done": False
    }
    response = requests.post(url, headers=headers, json=data)
    return response.json()


def main():
    # Создаем пользователей и тестовые данные
    for i, user_data in enumerate(USERS, 1):
        email = user_data["email"]
        password = user_data["password"]

        print(f"\nСоздаем пользователя {i}: {email}")

        # Регистрация
        register_response = register_user(email, password)
        print("Регистрация:", register_response)

        # Логин (получаем токен)
        login_response = login_user(email, password)
        token = login_response.get("access")
        print("Токен:", token[:50] + "...")

        if not token:
            print("Ошибка: не удалось получить токен")
            continue

        # Создаем категории
        categories = []
        for j in range(1, 4):
            cat_name = f"Категория {j} пользователя {i}"
            cat_response = create_category(token, cat_name)
            categories.append(cat_response)
            print(f"Создана категория: {cat_name} (ID: {cat_response.get('id')})")

        # Создаем задачи
        tasks = []
        for j in range(1, 4):
            deadline = (datetime.now() + timedelta(days=j)).isoformat() + "Z"
            task_content = f"Задача {j} пользователя {i}"
            cat_id = categories[j - 1]["id"] if j <= len(categories) else None

            task_response = create_task(
                token,
                content=task_content,
                deadline=deadline,
                priority=j,
                category_id=cat_id
            )
            tasks.append(task_response)
            print(f"Создана задача: {task_content} (ID: {task_response.get('id')})")

        # Создаем дополнительные подзадачи
        if tasks:
            for task in tasks:
                subtask_response = create_subtask(
                    token,
                    task_id=task["id"],
                    content=f"Дополнительная подзадача для задачи {task['id']}"
                )
                print(f"Создана подзадача: {subtask_response.get('content')}")

        # Получаем задачи с сортировкой по приоритету
        print("\nПолучаем задачи с сортировкой по приоритету:")
        sorted_tasks = get_tasks(token, {"sort_by": "priority"})
        for task in sorted_tasks:
            print(f"Задача: {task['content']}, приоритет: {task['dpc']['priority']}")

        # Получаем задачи с сортировкой по дедлайну
        print("\nПолучаем задачи с сортировкой по дедлайну:")
        sorted_tasks = get_tasks(token, {"sort_by": "deadline"})
        for task in sorted_tasks:
            print(f"Задача: {task['content']}, дедлайн: {task['dpc']['deadline']}")


if __name__ == "__main__":
    main()