from datetime import datetime, timezone

from django.test import SimpleTestCase, TestCase
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APIClient
from rest_framework_simplejwt.tokens import RefreshToken

from suggestion.service import SuggestionService
from taskbench.models.models import User, Category, Subscription


class SuggestionServiceTestCase(SimpleTestCase):
    def __init__(self, method_name: str = "runTest"):
        super().__init__(method_name)
        self.SuggestionService = SuggestionService(debug=True)

    def setUp(self):
        pass

    def test_deadline_suggestion(self):
        text = 'Не забыть, что завтра в 3 часа дня созвон'
        now_time = datetime(2025, 4, 24, 12, 00, 0).replace(tzinfo=None)
        supposed_time = datetime(2025, 4, 25, 15, 00, 0).replace(tzinfo=None)
        result = SuggestionService().suggest_deadline_local(text, now=now_time)
        print(result)
        self.assertEqual(result, supposed_time)

    def test_subtask_suggestion(self):
        text = 'Помыть машину'
        result = SuggestionService().suggest_subtasks(text)
        print(result)
        self.assertTrue(len(result) > 0)

    def test_category_suggestion(self):
        text = 'Завтра контрольная работа'
        category_names = ['семья', 'учеба', 'работа']
        result = SuggestionService().suggest_category(text, category_names)
        print("результат категории: ", category_names[result])
        self.assertTrue(-2 < result < len(category_names))

    def test_priority_suggestion(self):
        text = 'Очень важно не забыть помыть посуду'
        result = SuggestionService().suggest_priority(text)
        print('очень важно' if result == 1 else 'не очень важно')
        self.assertTrue(0 <= result <= 1)


class SuggestionApiTestCase(TestCase):
    def setUp(self):
        self.client = APIClient()

        another_user = User.objects.create(
            user_id=1003,
            email='testuser1003@mail.com'
        )
        another_user.set_password('password')
        another_user.save()

        self.user = User.objects.create(
            user_id=1002,
            email='testuser1002@mail.com'
        )
        self.user.set_password('test_password')
        self.user.save()

        self.user2 = User.objects.create(
            user_id=145,
            email='yet_another_user@mail.com'
        )
        self.user2.set_password('test_password')
        self.user2.save()

        self.sub = Subscription.objects.create(
            user=self.user,
        )
        self.sub.activate(0)

        self.access_token = RefreshToken.for_user(self.user).access_token
        self.access_token2 = RefreshToken.for_user(self.user2).access_token


        category1 = Category.objects.create(
            category_id=1001,
            user_id=1002,
            name='учеба'
        )

        category2 = Category.objects.create(
            category_id=1002,
            user_id=1002,
            name='работа'
        )

        category3 = Category.objects.create(
            category_id=1003,
            user_id=1003,
            name='не нужная категория'
        )

        self.category_names = [category1.name, category2.name]

    def test_suggestion_api(self):
        url = reverse('login')
        response = self.client.post(
            url,
            data={
                "email": "testuser1002@mail.com",
                "password": "test_password"
            }, format='json')
        token = response.json().get('access')
        now_time = datetime(2025, 4, 24, 12, 00, 0, tzinfo=timezone.utc)
        url = reverse('ai_suggestions')
        response = self.client.post(
            url,
            data={
                "dpc": {
                    "category_id": None,
                    "priority": None,
                    "deadline": None,
                },
                "title": "Не забыть, что завтра в 3 часа дня созвон",
                "timestamp": now_time,
            },
            HTTP_AUTHORIZATION=f'Bearer {token}',
            format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        subtasks = response.json().get('suggestions')
        self.assertTrue(len(subtasks) > 0)
        deadline = response.json().get('suggested_dpc').get('deadline')
        priority = int(response.json().get('suggested_dpc').get('priority'))
        self.assertIsNotNone(deadline)
        self.assertTrue(0 <= priority <= 1)

    def test_suggestion_unsubscribed_api(self):
        url = reverse('login')
        response = self.client.post(
            url,
            data={
                "email": "yet_another_user@mail.com",
                "password": "test_password"
            }, format='json')
        token = response.json().get('access')
        now_time = datetime(2025, 4, 24, 12, 00, 0, tzinfo=timezone.utc)
        url = reverse('ai_suggestions')
        response = self.client.post(
            url,
            data={
                "dpc": {
                    "category_id": None,
                    "priority": None,
                    "deadline": None,
                },
                "title": "Не забыть, что завтра в 3 часа дня созвон",
                "timestamp": now_time,
            },
            HTTP_AUTHORIZATION=f'Bearer {token}',
            format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        subtasks = response.json().get('suggestions')
        self.assertTrue(len(subtasks) == 0)
        deadline = response.json().get('suggested_dpc').get('deadline')
        self.assertIsNotNone(deadline)