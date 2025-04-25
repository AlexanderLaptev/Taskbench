import json

from django.test import TestCase
from django.urls import reverse
from rest_framework import status

from taskbench.models.models import User
from rest_framework.test import APIClient


class UserApiTests(TestCase):
    def setUp(self):
        self.client = APIClient()

        self.user = User.objects.create(
            email='example@mail.com',
        )
        self.user.set_password('test_password')
        self.user.save()

    def test_login_and_password_change(self):
        url = reverse('login')
        response = self.client.post(url,
                         data={
                             "email": "example@mail.com",
                             "password": "test_password"
                         }, format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIsNotNone(response.json().get('access'))
        self.assertIsNotNone(response.json().get('refresh'))

        access = str(response.json().get('access'))
        url = reverse('change_password')
        response = self.client.patch(url,
                                     data={
                                         "old_password": "test_password",
                                         "new_password": "new_password"
                                     },
                                     HTTP_AUTHORIZATION=f'Bearer {access}',
                                     format='json')
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)

        url = reverse('login')
        response = self.client.post(url,
                                    data={
                                        "email": "example@mail.com",
                                        "password": "new_password"
                                    },
                                    format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIsNotNone(response.json().get('access'))
        self.assertIsNotNone(response.json().get('refresh'))



    def test_register_refresh_and_delete(self):
        url = reverse('register')
        response = self.client.post(url,
                                    data={
                                        "email": 'new_email@mail.com',
                                        "password": "test_password"
                                    }, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertIsNotNone(response.json().get('access'))
        self.assertIsNotNone(response.json().get('refresh'))

        refresh = str(response.json().get('refresh'))

        url = reverse('token_refresh')
        response = self.client.post(url, data={"refresh": refresh}, format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIsNotNone(response.json().get('access'))
        access = str(response.json().get('access'))

        url = reverse('delete_user')
        response = self.client.delete(url, HTTP_AUTHORIZATION=f'Bearer {access}', format='json')
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
