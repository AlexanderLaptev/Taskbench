import json
import random
from locust import HttpUser, task, between, SequentialTaskSet

class UserBehavior(SequentialTaskSet):
    def on_start(self):
        self.client.verify = False  # Игнорировать SSL
        # Регистрация пользователя
        email = f"user{random.randint(1, 100000)}@test.com"
        self.client.post(
            "/user/register/",
            json={
                "email": email,
                "password": "test_password",
                "first_name": "Test",
                "last_name": "User"
            }
        )
        # Логин
        response = self.client.post(
            "/user/login/",
            json={"email": email, "password": "test_password"}
        )
        self.token = response.json().get("access")
        self.task_id = None

    @task(3)  # Статистика чаще всего запрашивается
    def get_statistics(self):
        self.client.get(
            "/statistics/",
            headers={"Authorization": f"Bearer {self.token}"}
        )

    @task(2)
    def create_category(self):
        response = self.client.post(
            "/categories/",
            json={"name": f"Category{random.randint(1, 1000)}"},
            headers={"Authorization": f"Bearer {self.token}"}
        )
        if response.status_code == 201:
            self.category_id = response.json().get("category_id")

    @task(2)
    def create_task(self):
        response = self.client.post(
            "/tasks/",
            json={
                "content": "Test task",
                "dpc": {
                    "deadline": "2025-05-30T14:00:00Z",
                    "priority": 2,
                    #"category_id": getattr(self, "category_id", None)
                },
                "subtasks": [{"content": "Subtask 1"}]
            },
            headers={"Authorization": f"Bearer {self.token}"}
        )
        if response.status_code == 201:
            self.task_id = response.json().get("task_id")

    @task(1)
    def create_subtask(self):
        if self.task_id:
            self.client.post(
                f"/subtasks/?task_id={self.task_id}",
                json={"content": "New subtask", "is_done": False},
                headers={"Authorization": f"Bearer {self.token}"}
            )

    @task(1)
    def complete_task(self):
        if self.task_id:
            self.client.delete(
                f"/tasks/{self.task_id}/",
                headers={"Authorization": f"Bearer {self.token}"}
            )

    #@task(1)
    def get_suggestions(self):
        self.client.post(
            "/ai/suggestions/",
            json={
                "title": "Prepare presentation",
                "deadline": "2025-05-30T14:00:00Z",
                "priority": 2,
                "timestamp": "2025-05-22T12:00:00Z"
            },
            headers={"Authorization": f"Bearer {self.token}"}
        )

class TaskbenchUser(HttpUser):
    tasks = [UserBehavior]
    wait_time = between(1, 5)  # Задержка 1–5 сек
    host = "https://193.135.137.154/"  # Замени на свою HTTPS-ссылку