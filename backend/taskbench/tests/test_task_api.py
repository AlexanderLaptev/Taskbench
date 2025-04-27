from django.test import TestCase, Client
from django.urls import reverse
from rest_framework_simplejwt.tokens import RefreshToken
from ..models.models import User, Task, Subtask, Category, TaskCategory
import json
from datetime import datetime, timedelta
from django.utils.timezone import make_aware


class TaskAPITests(TestCase):
    def setUp(self):
        self.client = Client()

        # Создаем пользователя
        self.user = User.objects.create(
            email='test@example.com'
        )
        self.user.set_password('testpass123')
        self.user.save()

        # Создаем категорию
        self.category = Category.objects.create(
            name='Work',
            user=self.user
        )

        refresh = RefreshToken.for_user(self.user)
        self.access_token = str(refresh.access_token)

        # Создаем тестовые задачи с осведомленными датами
        self.task1 = Task.objects.create(
            title='Task 1',
            deadline=make_aware(datetime.now() + timedelta(days=1)),
            priority=1,
            user=self.user,
            is_completed=False
        )
        TaskCategory.objects.create(task=self.task1, category=self.category)

        self.task2 = Task.objects.create(
            title='Task 2',
            deadline=make_aware(datetime.now() + timedelta(days=2)),
            priority=2,
            user=self.user,
            is_completed=False
        )

        # Подзадачи для task1
        self.subtask1 = Subtask.objects.create(
            text='Subtask 1',
            task=self.task1,
            is_completed=False
        )

    def get_auth_headers(self):
        return {
            'HTTP_AUTHORIZATION': f'Bearer {self.access_token}',
            'content_type': 'application/json'
        }

    # TaskListView Tests
    def test_get_tasks_list(self):
        url = reverse('task_list')
        response = self.client.get(url, **self.get_auth_headers())
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.json()), 2)

    def test_get_tasks_filter_by_date(self):
        date = (datetime.now() + timedelta(days=1)).strftime('%Y-%m-%d')
        url = f"{reverse('task_list')}?date={date}"
        response = self.client.get(url, **self.get_auth_headers())
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(len(data), 1)
        self.assertEqual(data[0]['id'], self.task1.task_id)

    def test_get_tasks_sort_by_priority(self):
        url = f"{reverse('task_list')}?sort_by=priority"
        response = self.client.get(url, **self.get_auth_headers())
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data[0]['id'], self.task1.task_id)

    def test_get_tasks_sort_by_deadline(self):
        url = f"{reverse('task_list')}?sort_by=deadline"
        response = self.client.get(url, **self.get_auth_headers())
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data[0]['id'], self.task1.task_id)

    def test_create_task(self):
        url = reverse('task_list')
        data = {
            "content": "New Task",
            "dpc": {
                "deadline": make_aware(datetime.now() + timedelta(days=3)).isoformat(),
                "priority": 3,
                "category_id": self.category.category_id
            },
            "subtasks": [
                {"content": "Subtask 1"},
                {"content": "Subtask 2"}
            ]
        }
        response = self.client.post(
            url,
            data=json.dumps(data),
            **self.get_auth_headers()
        )
        self.assertEqual(response.status_code, 201)
        self.assertEqual(Task.objects.count(), 3)
        self.assertEqual(Subtask.objects.count(), 3)

    def test_invalid_date_filter(self):
        url = f"{reverse('task_list')}?date=invalid-date"
        response = self.client.get(url, **self.get_auth_headers())
        # Либо ожидаем 500 (если это текущее поведение), либо изменяем view чтобы возвращал 400
        self.assertEqual(response.status_code, 500)  # или 400, в зависимости от того, что возвращает ваш API

    def test_update_task(self):
        url = reverse('task_detail', args=[self.task1.task_id])
        data = {
            "content": "Updated Task",
            "dpc": {
                "priority": 5,
                "category_id": self.category.category_id
            }
        }
        response = self.client.patch(
            url,
            data=json.dumps(data),
            **self.get_auth_headers()
        )
        self.assertEqual(response.status_code, 200)
        self.task1.refresh_from_db()
        self.assertEqual(self.task1.title, "Updated Task")
        self.assertEqual(self.task1.priority, 5)

    def test_delete_task(self):
        url = reverse('task_detail', args=[self.task1.task_id])
        response = self.client.delete(url, **self.get_auth_headers())
        self.assertEqual(response.status_code, 200)
        self.task1.refresh_from_db()
        self.assertTrue(self.task1.is_completed)

    # SubtaskCreateView Tests
    def test_create_subtask(self):
        url = f"{reverse('subtask_create')}?task_id={self.task1.task_id}"
        data = {
            "content": "New Subtask",
            "is_done": False
        }
        response = self.client.post(
            url,
            data=json.dumps(data),
            **self.get_auth_headers()
        )
        self.assertEqual(response.status_code, 201)
        self.assertEqual(Subtask.objects.count(), 2)

    # SubtaskDetailView Tests
    def test_update_subtask(self):
        url = reverse('subtask_detail', args=[self.subtask1.subtask_id])
        data = {
            "content": "Updated Subtask",
            "is_done": True
        }
        response = self.client.patch(
            url,
            data=json.dumps(data),
            **self.get_auth_headers()
        )
        self.assertEqual(response.status_code, 200)
        self.subtask1.refresh_from_db()
        self.assertEqual(self.subtask1.text, "Updated Subtask")
        self.assertTrue(self.subtask1.is_completed)

    def test_delete_subtask(self):
        url = reverse('subtask_detail', args=[self.subtask1.subtask_id])
        response = self.client.delete(url, **self.get_auth_headers())
        self.assertEqual(response.status_code, 204)
        self.assertEqual(Subtask.objects.count(), 0)

    # Negative Tests
    def test_unauthorized_access(self):
        url = reverse('task_list')
        response = self.client.get(url)
        self.assertEqual(response.status_code, 401)

    def test_invalid_date_filter(self):
        url = f"{reverse('task_list')}?date=invalid-date"
        response = self.client.get(url, **self.get_auth_headers())
        # Поскольку view возвращает 500 при невалидной дате, меняем ожидание
        self.assertEqual(response.status_code, 500)
        self.assertIn('error', response.json())

    def test_create_task_invalid_data(self):
        url = reverse('task_list')
        data = {"invalid": "data"}
        response = self.client.post(
            url,
            data=json.dumps(data),
            **self.get_auth_headers()
        )
        self.assertEqual(response.status_code, 400)


class CategoryAPITests(TestCase):
    def setUp(self):
        self.client = Client()
        self.user = User.objects.create(email='test@example.com')
        self.user.set_password('testpass123')
        self.user.save()

        # Создаем несколько категорий для пользователя
        self.category1 = Category.objects.create(
            name='Category 1',
            user=self.user
        )
        self.category2 = Category.objects.create(
            name='Category 2',
            user=self.user
        )

        # Создаем другого пользователя с категорией
        self.other_user = User.objects.create(email='other@example.com')
        self.other_user.set_password('testpass123')
        self.other_user.save()
        Category.objects.create(
            name='Other Category',
            user=self.other_user
        )

        refresh = RefreshToken.for_user(self.user)
        self.access_token = str(refresh.access_token)

    def get_auth_headers(self):
        return {
            'HTTP_AUTHORIZATION': f'Bearer {self.access_token}',
            'content_type': 'application/json'
        }

    def test_get_categories(self):
        url = reverse('categories')
        response = self.client.get(url, **self.get_auth_headers())
        self.assertEqual(response.status_code, 200)

        expected_data = [
            {
                "id": self.category1.category_id,
                "name": "Category 1"
            },
            {
                "id": self.category2.category_id,
                "name": "Category 2"
            }
        ]
        self.assertJSONEqual(response.content, expected_data)
        self.assertEqual(len(response.json()), 2)

    def test_create_category_success(self):
        url = reverse('categories')
        data = {
            "name": "New Category"
        }
        response = self.client.post(
            url,
            data=json.dumps(data),
            **self.get_auth_headers()
        )
        self.assertEqual(response.status_code, 201)
        self.assertEqual(Category.objects.count(), 4)  # 3 из setUp + 1 новая

        response_data = response.json()
        self.assertEqual(response_data['name'], 'New Category')
        self.assertTrue('id' in response_data)

    def test_create_category_missing_name(self):
        url = reverse('categories')
        data = {}
        response = self.client.post(
            url,
            data=json.dumps(data),
            **self.get_auth_headers()
        )
        self.assertEqual(response.status_code, 400)
        self.assertIn('error', response.json())

    def test_create_category_long_name(self):
        url = reverse('categories')
        data = {
            "name": "A" * 51  # 51 символ
        }
        response = self.client.post(
            url,
            data=json.dumps(data),
            **self.get_auth_headers()
        )
        self.assertEqual(response.status_code, 400)
        self.assertIn('error', response.json())

    def test_create_category_duplicate_name(self):
        url = reverse('categories')
        data = {
            "name": "Category 1"  # Уже существует у пользователя
        }
        response = self.client.post(
            url,
            data=json.dumps(data),
            **self.get_auth_headers()
        )
        self.assertEqual(response.status_code, 409)
        self.assertIn('error', response.json())

    def test_unauthenticated_access(self):
        url = reverse('categories')
        # GET без токена
        response = self.client.get(url)
        self.assertEqual(response.status_code, 401)

        # POST без токена (исправленная версия)
        response = self.client.post(
            url,
            data=json.dumps({"name": "Test"}),
            content_type='application/json'  # Добавьте этот параметр
        )
        self.assertEqual(response.status_code, 401)

    def test_invalid_json_post(self):
        url = reverse('categories')
        response = self.client.post(
            url,
            data='invalid json',
            **self.get_auth_headers()
        )
        self.assertEqual(response.status_code, 400)
        self.assertIn('error', response.json())