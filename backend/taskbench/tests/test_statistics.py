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

        self.user = User.objects.create(
            email='example1@mail.com',
        )
        self.user.set_password('test_password')
        self.user.save()

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

        today = timezone.now()
        yesterday = today - timedelta(days=1)
        two_days_ago = today - timedelta(days=2)

        for i in range(3):
            Task.objects.create(
                user=self.user,
                title=f"Today's task {i}",
                is_completed=True,
                completed_at=today
            )

        for i in range(5):
            Task.objects.create(
                user=self.user,
                title=f"Yesterday's task {i}",
                is_completed=True,
                completed_at=yesterday
            )

        for i in range(2):
            Task.objects.create(
                user=self.user,
                title=f"Two days ago task {i}",
                is_completed=True,
                completed_at=two_days_ago
            )

        Task.objects.create(
            user=self.user,
            title="Incomplete task",
            is_completed=False
        )

        response = self.client.get(url, HTTP_AUTHORIZATION=f'Bearer {self.get_jwt()}')
        self.assertEqual(response.status_code, 200)

        data = response.json()

        self.assertEqual(data['done_today'], 3)

        self.assertEqual(data['max_done'], 5)

        today_index = 6
        yesterday_index = 5

        self.assertAlmostEqual(data['weekly'][today_index], 0.6, places=1)

        self.assertAlmostEqual(data['weekly'][yesterday_index], 1.0, places=1)

    def test_different_users_statistics(self):
        url = reverse('statistics')
        user2 = User.objects.create(
            email='testuser2@example.com'
        )
        user2.set_password('testpassword')
        user2.save()

        access_token2 = self.get_jwt(email=user2.email, password='testpassword')
        access_token = self.get_jwt()

        today = timezone.now()

        for i in range(3):
            Task.objects.create(
                user=self.user,
                title=f"User1 task {i}",
                is_completed=True,
                completed_at=today
            )

        for i in range(5):
            Task.objects.create(
                user=user2,
                title=f"User2 task {i}",
                is_completed=True,
                completed_at=today
            )

        response1 = self.client.get(url, HTTP_AUTHORIZATION=f'Bearer {access_token}')

        self.assertEqual(response1.status_code, 200)
        data1 = response1.json()

        response2 = self.client.get(url, HTTP_AUTHORIZATION=f'Bearer {access_token2}')


        self.assertEqual(response2.status_code, 200)
        data2 = response2.json()

        self.assertEqual(data1['done_today'], 3)
        self.assertEqual(data2['done_today'], 5)

        self.assertEqual(data1['max_done'], 3)
        self.assertEqual(data2['max_done'], 5)

        self.assertNotEqual(data1, data2)

    def test_invalid_token(self):
        url = reverse('statistics')
        response = self.client.get(url, HTTP_AUTHORIZATION=f'Bearer invalid_token')

        self.assertEqual(response.status_code, 401)  # Unauthorized

    def get_jwt(self, email='example1@mail.com', password='test_password'):
        url = reverse('login')
        response = self.client.post(url,
                                    data={
                                        "email": email,
                                        "password": password,
                                    }, format='json')
        return response.json().get('access')