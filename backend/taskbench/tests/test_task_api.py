from django.test import TestCase
from django.urls import reverse
from rest_framework.test import APIClient
from ..models.models import Task, Subtask, TaskCategory, Category, User
import json
from datetime import datetime, timedelta
from django.utils.timezone import make_aware

#docker compose exec taskbench-backend python manage.py test taskbench.test_api

class TaskAPITests(TestCase):
    def setUp(self):
        self.client = APIClient()

        # Создаем тестового пользователя
        self.user = User.objects.create(
            user_id=1,
            username='testuser'
        )

        # Создаем тестовые данные с привязкой к пользователю
        self.task = Task.objects.create(
            title="Тестовая задача",
            deadline=make_aware(datetime.now() + timedelta(days=1)),
            priority=1,
            status='active',
            user=self.user  # Используем созданного пользователя
        )

        self.category = Category.objects.create(
            name="Работа",
            user=self.user  # Используем созданного пользователя
        )

        TaskCategory.objects.create(task=self.task, category=self.category)
        self.subtask = Subtask.objects.create(
            text="Тестовая подзадача",
            task=self.task,
            is_completed=False
        )

    # Тесты для /tasks/ (GET, POST)
    def test_get_tasks(self):
        url = reverse('task_list')
        response = self.client.get(url + '?user_id=1')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.json()), 1)
        self.assertEqual(response.json()[0]['content'], "Тестовая задача")

    def test_get_tasks_with_filters(self):
        url = reverse('task_list')
        # Тестируем фильтрацию по категории
        response = self.client.get(url + f'?user_id=1&category_id={self.category.category_id}')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.json()), 1)

        # Тестируем сортировку
        response = self.client.get(url + '?user_id=1&sort_by=priority')
        self.assertEqual(response.status_code, 200)

    def test_create_task(self):
        url = reverse('task_list')
        data = {
            "content": "Новая задача",
            "dpc": {
                "deadline": "2025-05-25T14:00:00Z",
                "priority": 2,
                "category_id": self.category.category_id
            },
            "subtasks": [
                {"content": "Подзадача 1"},
                {"content": "Подзадача 2"}
            ]
        }
        response = self.client.post(url + '?user_id=1', data, format='json')
        self.assertEqual(response.status_code, 201)
        self.assertEqual(Task.objects.count(), 2)
        self.assertEqual(Subtask.objects.count(), 3)  # 1 существующая + 2 новые

    # Тесты для /tasks/<task_id>/ (PATCH, DELETE)
    def test_update_task(self):
        url = reverse('task_detail', args=[self.task.task_id])
        data = {
            "content": "Обновленная задача",
            "dpc": {
                "priority": 3
            }
        }
        response = self.client.patch(url, data, format='json')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()['content'], "Обновленная задача")
        self.assertEqual(response.json()['dpc']['priority'], 3)

    def test_mark_task_completed(self):
        url = reverse('task_detail', args=[self.task.task_id])
        response = self.client.delete(url)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()['is_done'], True)
        self.task.refresh_from_db()
        self.assertEqual(self.task.status, 'completed')

    # Тесты для /subtasks/ (POST)
    def test_create_subtask(self):
        url = reverse('subtask_create')
        data = {
            "content": "Новая подзадача",
            "is_done": True
        }
        response = self.client.post(url + f'?task_id={self.task.task_id}', data, format='json')
        self.assertEqual(response.status_code, 201)
        self.assertEqual(Subtask.objects.count(), 2)
        self.assertEqual(response.json()['is_done'], True)

    # Тесты для /subtasks/<subtask_id>/ (PATCH, DELETE)
    def test_update_subtask(self):
        url = reverse('subtask_detail', args=[self.subtask.subtask_id])
        data = {
            "content": "Обновленная подзадача",
            "is_done": True
        }
        response = self.client.patch(url, data, format='json')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()['content'], "Обновленная подзадача")
        self.assertEqual(response.json()['is_done'], True)

    def test_delete_subtask(self):
        url = reverse('subtask_detail', args=[self.subtask.subtask_id])
        response = self.client.delete(url)
        self.assertEqual(response.status_code, 204)
        self.assertEqual(Subtask.objects.count(), 0)

    # Тесты обработки ошибок
    def test_task_not_found(self):
        url = reverse('task_detail', args=[999])
        response = self.client.get(url)
        self.assertEqual(response.status_code, 404)

    def test_create_task_missing_user_id(self):
        url = reverse('task_list')
        data = {"content": "Задача без user_id"}
        response = self.client.post(url, data, format='json')
        self.assertEqual(response.status_code, 400)

    def test_invalid_json(self):
        url = reverse('task_list')
        response = self.client.post(url + '?user_id=1', "{invalid json}", content_type='application/json')
        self.assertEqual(response.status_code, 400)

    def test_create_task_without_subtasks(self):
        url = reverse('task_list')
        data = {
            "content": "Задача без подзадач",
            "dpc": {
                "deadline": "2025-05-25T14:00:00Z",
                "priority": 2
            }
        }
        response = self.client.post(url + '?user_id=1', data, format='json')
        self.assertEqual(response.status_code, 201)
        self.assertEqual(Subtask.objects.count(), 1)  # Проверяем, что не создались лишние подзадачи

    def test_create_task_invalid_deadline(self):
        url = reverse('task_list')
        data = {
            "content": "Задача с невалидной датой",
            "dpc": {
                "deadline": "не-дата",
                "priority": 2
            }
        }
        response = self.client.post(url + '?user_id=1', data, format='json')
        self.assertEqual(response.status_code, 400)

    def test_pagination(self):
        # Создаем несколько дополнительных задач с обязательными полями
        for i in range(15):
            Task.objects.create(
                title=f"Задача {i}",
                deadline=make_aware(datetime.now() + timedelta(days=i)),  # Добавляем deadline
                priority=i % 3 + 1,  # Добавляем priority
                user=self.user,
                status='active'
            )

        url = reverse('task_list')
        response = self.client.get(url + '?user_id=1&limit=5&offset=5')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.json()), 5)

    def test_partial_update_subtask(self):
        url = reverse('subtask_detail', args=[self.subtask.subtask_id])
        # Обновляем только статус
        data = {"is_done": True}
        response = self.client.patch(url, data, format='json')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()['is_done'], True)
        self.assertEqual(response.json()['content'], "Тестовая подзадача")  # Проверяем, что текст не изменился

    def test_category_consistency_after_task_update(self):
        new_category = Category.objects.create(name="Дом", user=self.user)
        url = reverse('task_detail', args=[self.task.task_id])
        data = {
            "dpc": {
                "category_id": new_category.category_id
            }
        }
        response = self.client.patch(url, data, format='json')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()['dpc']['category_id'], new_category.category_id)
        self.assertEqual(TaskCategory.objects.filter(task=self.task).count(), 1)  # Проверяем, что старая связь удалена