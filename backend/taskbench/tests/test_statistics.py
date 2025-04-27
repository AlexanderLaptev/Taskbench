from django.test import TestCase
from django.urls import reverse
from rest_framework.test import APIClient
from rest_framework_simplejwt.tokens import RefreshToken
from ..models.models import User, Task
from django.utils import timezone
from datetime import timedelta


class StatisticsAPITests(TestCase):
    def setUp(self):
        self.client = APIClient()

        # Создаем тестового пользователя
        self.user = User.objects.create(
            email='testuser@example.com'
        )
        self.user.set_password('testpassword')
        self.user.save()

        # Получаем JWT токен для аутентификации
        refresh = RefreshToken.for_user(self.user)
        self.access_token = str(refresh.access_token)

    def test_authentication_required(self):
        """Проверяет, что API требует аутентификации"""
        url = reverse('statistics')
        response = self.client.get(url)
        self.assertEqual(response.status_code, 401)  # Unauthorized

    def test_empty_statistics(self):
        """Проверяет ответ API, когда у пользователя нет задач"""
        url = reverse('statistics')
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.access_token}')
        response = self.client.get(url)

        self.assertEqual(response.status_code, 200)
        data = response.json()

        # Проверяем соответствие формату API
        self.assertEqual(data['done_today'], 0)
        self.assertEqual(data['max_done'], 0)
        self.assertEqual(len(data['weekly']), 7)

        # Проверяем, что все значения массива weekly равны 0.0
        for value in data['weekly']:
            self.assertEqual(value, 0.0)

    def test_statistics_calculation(self):
        """Проверяет правильность расчета статистики"""
        url = reverse('statistics')
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.access_token}')

        # Создаем тестовые задачи
        today = timezone.now()
        yesterday = today - timedelta(days=1)
        two_days_ago = today - timedelta(days=2)

        # 3 задачи выполнены сегодня
        for i in range(3):
            task = Task.objects.create(
                user=self.user,
                title=f"Today's task {i}",
                is_completed=True,
                completed_at=today
            )

        # 5 задач выполнены вчера
        for i in range(5):
            task = Task.objects.create(
                user=self.user,
                title=f"Yesterday's task {i}",
                is_completed=True,
                completed_at=yesterday
            )

        # 2 задачи выполнены позавчера
        for i in range(2):
            task = Task.objects.create(
                user=self.user,
                title=f"Two days ago task {i}",
                is_completed=True,
                completed_at=two_days_ago
            )

        # Одна невыполненная задача (не должна учитываться)
        Task.objects.create(
            user=self.user,
            title="Incomplete task",
            is_completed=False
        )

        # Запрашиваем статистику
        response = self.client.get(url)
        self.assertEqual(response.status_code, 200)

        data = response.json()

        # Проверяем количество выполненных задач сегодня
        self.assertEqual(data['done_today'], 3)

        # Проверяем максимальное количество выполненных задач за день
        self.assertEqual(data['max_done'], 5)

        # Расчет индексов для сегодня и вчера в массиве weekly
        today_weekday = today.weekday()
        today_index = (today_weekday + 1) % 7  # Преобразуем в формат 0=воскресенье
        yesterday_index = (today_weekday) % 7  # Вчерашний день

        # Проверяем продуктивность для сегодня (3/5 = 0.6)
        self.assertAlmostEqual(data['weekly'][today_index], 0.6, places=1)

        # Проверяем продуктивность для вчера (5/5 = 1.0)
        self.assertAlmostEqual(data['weekly'][yesterday_index], 1.0, places=1)

    def test_different_users_statistics(self):
        """Проверяет, что у разных пользователей разная статистика"""
        # Создаем второго пользователя
        user2 = User.objects.create(
            email='testuser2@example.com'
        )
        user2.set_password('testpassword')
        user2.save()

        # Создаем токен для второго пользователя
        refresh2 = RefreshToken.for_user(user2)
        access_token2 = str(refresh2.access_token)

        today = timezone.now()

        # Задачи для первого пользователя (3)
        for i in range(3):
            Task.objects.create(
                user=self.user,
                title=f"User1 task {i}",
                is_completed=True,
                completed_at=today
            )

        # Задачи для второго пользователя (5)
        for i in range(5):
            Task.objects.create(
                user=user2,
                title=f"User2 task {i}",
                is_completed=True,
                completed_at=today
            )

        # Проверяем статистику первого пользователя
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.access_token}')
        response1 = self.client.get(reverse('statistics'))

        self.assertEqual(response1.status_code, 200)
        data1 = response1.json()

        # Проверяем статистику второго пользователя
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {access_token2}')
        response2 = self.client.get(reverse('statistics'))

        self.assertEqual(response2.status_code, 200)
        data2 = response2.json()

        # Проверяем, что количество выполненных задач соответствует ожиданиям
        self.assertEqual(data1['done_today'], 3)
        self.assertEqual(data2['done_today'], 5)

        # Проверяем, что максимальное количество соответствует ожиданиям
        self.assertEqual(data1['max_done'], 3)
        self.assertEqual(data2['max_done'], 5)

        # Проверяем, что статистики разные
        self.assertNotEqual(data1, data2)

    def test_invalid_token(self):
        """Проверяет обработку недействительного токена"""
        url = reverse('statistics')
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer invalid_token')
        response = self.client.get(url)

        self.assertEqual(response.status_code, 401)  # Unauthorized