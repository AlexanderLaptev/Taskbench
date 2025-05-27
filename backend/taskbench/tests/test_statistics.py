from django.test import TestCase
from django.urls import reverse
from rest_framework.test import APIClient
from rest_framework_simplejwt.tokens import RefreshToken
from taskbench.models.models import User, Task
from django.utils import timezone
from datetime import timedelta, datetime


class StatisticsAPITests(TestCase):
    def setUp(self):
        self.client = APIClient()
        self.user = User.objects.create(
            email='example1@mail.com',
        )
        self.user.set_password('test_password')
        self.user.save()

    def get_jwt(self, email='example1@mail.com', password='test_password'):
        url = reverse('login')
        response = self.client.post(url, data={
            "email": email,
            "password": password,
        }, format='json')
        return response.json().get('access')

    def test_authentication_required(self):
        url = reverse('statistics')
        response = self.client.get(url)
        self.assertEqual(response.status_code, 401)

    def test_empty_statistics(self):
        url = reverse('statistics')
        response = self.client.get(url, HTTP_AUTHORIZATION=f'Bearer {self.get_jwt()}')
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data['done_today'], 0)
        self.assertEqual(data['max_done'], 0)
        self.assertEqual(len(data['weekly']), 7)
        for value in data['weekly']:
            self.assertEqual(value, 0.0)

    def test_statistics_calculation(self):
        url = reverse('statistics')
        today = timezone.now().date()
        start_of_week = today - timedelta(days=today.weekday())  # Понедельник
        monday = timezone.make_aware(datetime.combine(start_of_week, datetime.min.time()))
        tuesday = monday + timedelta(days=1)

        # Проверяем, является ли сегодня понедельником (weekday() == 0)
        is_monday = today.weekday() == 0

        # Создаём задачи в зависимости от дня недели
        if is_monday:
            # Если понедельник, создаём только задачи на понедельник
            Task.objects.create(user=self.user, title="Monday task 1", is_completed=True, completed_at=monday)
            Task.objects.create(user=self.user, title="Monday task 2", is_completed=True, completed_at=monday)
        else:
            # Если не понедельник, создаём задачи на понедельник и вторник
            Task.objects.create(user=self.user, title="Monday task 1", is_completed=True, completed_at=monday)
            Task.objects.create(user=self.user, title="Monday task 2", is_completed=True, completed_at=monday)
            Task.objects.create(user=self.user, title="Tuesday task 1", is_completed=True, completed_at=tuesday)
            Task.objects.create(user=self.user, title="Tuesday task 2", is_completed=False, completed_at=None)

        response = self.client.get(url, HTTP_AUTHORIZATION=f'Bearer {self.get_jwt()}')
        self.assertEqual(response.status_code, 200)
        data = response.json()

        print(f"Data: {data}")  # Отладочный вывод
        self.assertEqual(data['max_done'], 2)  # Максимум задач в понедельник

        # Исправляем ожидание для done_today
        if is_monday:
            self.assertEqual(data['done_today'], 2)  # Сегодня понедельник, задачи на понедельник
        else:
            self.assertEqual(data['done_today'], 1)  # Сегодня вторник, 1 задача на вторник

        self.assertEqual(len(data['weekly']), 7)
        self.assertAlmostEqual(data['weekly'][0], 1.0)  # Понедельник: 2/2

        if is_monday:
            # Если сегодня понедельник, вторник и остальные дни должны быть 0.0
            for i in range(1, 7):
                self.assertAlmostEqual(data['weekly'][i], 0.0)  # Остальные дни: 0/2
        else:
            # Если не понедельник, вторник должен быть 0.5, а остальные дни — 0.0
            self.assertAlmostEqual(data['weekly'][1], 0.5)  # Вторник: 1/2 (1 завершённая, максимум 2)
            for i in range(2, 7):
                self.assertAlmostEqual(data['weekly'][i], 0.0)  # Остальные дни: 0/2


    def test_different_users_statistics(self):
        url = reverse('statistics')
        user2 = User.objects.create(email='testuser2@example.com')
        user2.set_password('testpassword')
        user2.save()

        today = timezone.now().date()
        start_of_week = today - timedelta(days=today.weekday())
        monday = timezone.make_aware(datetime.combine(start_of_week, datetime.min.time()))

        # Задачи для первого пользователя
        Task.objects.create(
            user=self.user,
            title="User1 task",
            is_completed=True,
            completed_at=monday
        )

        # Задачи для второго пользователя
        Task.objects.create(
            user=user2,
            title="User2 task 1",
            is_completed=True,
            completed_at=monday
        )
        Task.objects.create(
            user=user2,
            title="User2 task 2",
            is_completed=True,
            completed_at=monday
        )

        access_token1 = self.get_jwt()
        access_token2 = self.get_jwt(email='testuser2@example.com', password='testpassword')

        response1 = self.client.get(url, HTTP_AUTHORIZATION=f'Bearer {access_token1}')
        response2 = self.client.get(url, HTTP_AUTHORIZATION=f'Bearer {access_token2}')

        self.assertEqual(response1.status_code, 200)
        self.assertEqual(response2.status_code, 200)

        data1 = response1.json()
        data2 = response2.json()

        self.assertEqual(data1['done_today'], 0 if today != start_of_week else 1)
        self.assertEqual(data2['done_today'], 0 if today != start_of_week else 2)
        self.assertEqual(data1['max_done'], 1)
        self.assertEqual(data2['max_done'], 2)
        self.assertAlmostEqual(data1['weekly'][0], 1.0)
        self.assertAlmostEqual(data2['weekly'][0], 1.0)

    def test_invalid_token(self):
        url = reverse('statistics')
        response = self.client.get(url, HTTP_AUTHORIZATION='Bearer invalid_token')
        self.assertEqual(response.status_code, 401)