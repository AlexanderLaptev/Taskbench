from django.test import TestCase, Client
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APIClient

from taskbench.models.models import User, Subscription


class SubscriptionAPITests(TestCase):
    def setUp(self):
        self.client = APIClient()

        self.user1 = User.objects.create(
            email='test_for_sub@example.com'
        )
        self.user1.set_password('testpass123')
        self.user1.save()

        self.user2 = User.objects.create(
            email='test_for_sub2@example.com'
        )
        self.user2.set_password('testpass123')
        self.user2.save()

        self.user3 = User.objects.create(
            email='test_for_sub3@example.com'
        )
        self.user3.set_password('testpass123')
        self.user3.save()

        self.user4 = User.objects.create(
            email='test_for_sub4@example.com'
        )
        self.user4.set_password('testpass123')
        self.user4.save()

        self.subscription2 = Subscription.objects.create(
            user=self.user2,
        )
        self.subscription2.activate(0)

        self.subscription3 = Subscription.objects.create(
            user=self.user3,
        )
        self.subscription3.activate(1)

        self.subscription4 = Subscription.objects.create(
            user=self.user4,
        )
        self.subscription4.activate(2)
        self.subscription4.deactivate()

    def test_unsubscribed(self):
        url = reverse('login')
        response = self.client.post(
            url,
            data={
                "email": "test_for_sub@example.com",
                "password": "testpass123"
            },
            format='json')
        access = str(response.json().get('access'))

        url = reverse('subscription_status')
        response = self.client.get(
            url,
            HTTP_AUTHORIZATION=f'Bearer {access}',
            format='json'
        )

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        is_subscribed = bool(response.json().get('is_subscribed'))
        self.assertFalse(is_subscribed)

    def test_subscribed(self):
        url = reverse('login')
        response = self.client.post(
            url,
            data={
                "email": "test_for_sub2@example.com",
                "password": "testpass123"
            },
            format='json')
        access = str(response.json().get('access'))

        url = reverse('subscription_status')
        response = self.client.get(
            url,
            HTTP_AUTHORIZATION=f'Bearer {access}',
            format='json')

        self.assertEqual(response.status_code, status.HTTP_200_OK)
        subscription_id = int(response.json().get('subscription_id'))
        is_subscribed = bool(response.json().get('is_subscribed'))
        self.assertTrue(is_subscribed)
        self.assertEqual(subscription_id, self.subscription2.subscription_id)

    def test_deactivate(self):
        url = reverse('login')
        response = self.client.post(
            url,
            data={
                "email": "test_for_sub3@example.com",
                "password": "testpass123"
            },
            format='json')
        access = str(response.json().get('access'))

        url = reverse('subscription_status')
        response = self.client.get(
            url,
            HTTP_AUTHORIZATION=f'Bearer {access}',
            format='json')
        is_active = bool(response.json().get('is_active'))
        self.assertTrue(is_active)

        url = reverse('manage_subscription')
        response = self.client.delete(
            url,
            HTTP_AUTHORIZATION=f'Bearer {access}',
            format='json')
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)

        url = reverse('subscription_status')
        response = self.client.get(
            url,
            HTTP_AUTHORIZATION=f'Bearer {access}',
            format='json')
        is_active = bool(response.json().get('is_active'))
        is_subscribed = bool(response.json().get('is_subscribed'))
        self.assertFalse(is_active)
        self.assertTrue(is_subscribed)

    def test_activate(self):
        url = reverse('login')
        response = self.client.post(
            url,
            data={
                "email": "test_for_sub4@example.com",
                "password": "testpass123"
            },
            format='json')
        access = str(response.json().get('access'))

        url = reverse('subscription_status')
        response = self.client.get(
            url,
            HTTP_AUTHORIZATION=f'Bearer {access}',
            format='json')
        is_active = bool(response.json().get('is_active'))
        self.assertFalse(is_active)

        url = reverse('manage_subscription')
        response = self.client.post(
            url,
            HTTP_AUTHORIZATION=f'Bearer {access}',
            format='json')
        subscription_id = int(response.json().get('subscription_id'))
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(subscription_id, self.subscription4.subscription_id)

        url = reverse('subscription_status')
        response = self.client.get(
            url,
            HTTP_AUTHORIZATION=f'Bearer {access}',
            format='json')
        is_active = bool(response.json().get('is_active'))
        self.assertTrue(is_active)
