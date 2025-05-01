import json

from django.http import JsonResponse
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken
from ..models.models import User, Subscription

from ..serializers.user_serializers import UserRegisterSerializer, LoginSerializer, JwtSerializer
from ..services.jwt_service import get_token_from_request

from datetime import datetime, timedelta
import uuid
from django.utils import timezone


class RegisterView(APIView):

    def post(self, request, *args, **kwargs):
        data = json.loads(request.body)
        serializer = UserRegisterSerializer(data=data)
        if serializer.is_valid():
            user = serializer.save()

            refresh = RefreshToken.for_user(user)
            access_token = str(refresh.access_token)

            return JsonResponse({
                'user_id': user.user_id,
                'access': access_token,
                'refresh': str(refresh),
            }, status=201)
        return JsonResponse(serializer.errors, status=400)


class LoginView(APIView):

    def post(self, request, *args, **kwargs):
        data = json.loads(request.body)
        serializer = LoginSerializer(data=data)

        if serializer.is_valid():
            user = serializer.validated_data['user']
            refresh = RefreshToken.for_user(user)
            access_token = str(refresh.access_token)
            refresh_token = str(refresh)
            return JsonResponse({
                'user_id': user.user_id,
                'access': access_token,
                'refresh': refresh_token
            }, status=200)

        return Response(serializer.errors, status=400)

class DeleteUserView(APIView):

    def delete(self, request, *args, **kwargs):
        token = get_token_from_request(request)
        serializer = JwtSerializer(data=token)
        if serializer.is_valid():
            user = serializer.validated_data['user']
            user.delete()
            return Response(status=204)
        return Response(serializer.errors, status=400)
        # user = request.user
        # user.delete()
        # return Response(status=204)


class ChangePasswordView(APIView):

    def patch(self, request, *args, **kwargs):
        old_password = request.data.get('old_password')
        new_password = request.data.get('new_password')

        if not old_password or not new_password:
            return JsonResponse(
                {"error": "Both old_password and new_password are required"},
                status=400
            )

        token = get_token_from_request(request)
        serializer = JwtSerializer(data=token)
        if not serializer.is_valid():
            return JsonResponse(
                {"error": "Not logged in"},
                status=401
            )
        user = serializer.validated_data['user']

        if not user.check_password(old_password):
            return JsonResponse(
                {"error": "Old password is incorrect"},
                status=400
            )

        user.set_password(new_password)
        user.save()

        return Response(status=204)

class TokenRefreshView(APIView):

    def post(self, request, *args, **kwargs):
        data = json.loads(request.body)
        token = {'token': str(data['refresh'])}
        if data['refresh'] is None:
            return Response('No token provided.', status=400)

        serializer = JwtSerializer(data=token)
        if serializer.is_valid():
            user = serializer.validated_data['user']
            refresh = RefreshToken.for_user(user)
            access_token = str(refresh.access_token)
            return JsonResponse({
                'access': access_token,
                'user_id': user.user_id,
                'refresh': str(refresh),
            }, status=200)
        return Response(serializer.errors, status=400)


class SubscriptionStatusView(APIView):
    def get(self, request):
        # Проверка JWT токена
        token = get_token_from_request(request)
        serializer = JwtSerializer(data=token)
        if not serializer.is_valid():
            return JsonResponse({'error': 'Invalid token'}, status=401)
        user = serializer.validated_data['user']

        # Проверяем активную подписку
        now = timezone.now()
        active_subscription = user.subscriptions.filter(
            is_active=True,
            start_date__lte=now,
            end_date__gte=now
        ).first()

        # Формируем ответ
        if active_subscription:
            return JsonResponse({
                'has_subscription': True,
                'start_date': active_subscription.start_date,
                'end_date': active_subscription.end_date,
                'transaction_id': active_subscription.transaction_id
            })
        else:
            return JsonResponse({
                'has_subscription': False
            })


class CreateSubscriptionView(APIView):
    def post(self, request):
        # Проверка JWT токена
        token = get_token_from_request(request)
        serializer = JwtSerializer(data=token)
        if not serializer.is_valid():
            return JsonResponse({'error': 'Invalid token'}, status=401)
        user = serializer.validated_data['user']

        # Создаем тестовую подписку
        now = timezone.now()
        end_date = now + timedelta(days=30)  # Подписка на 1 месяц

        # В CreateSubscriptionView перед созданием
        if user.subscriptions.filter(is_active=True, end_date__gte=now).exists():
            return JsonResponse({'error': 'User already has active subscription'}, status=400)

        subscription = Subscription.objects.create(
            user=user,
            start_date=now,
            end_date=end_date,
            is_active=True,
            transaction_id=str(uuid.uuid4())  # Генерируем случайный transaction_id
        )

        return JsonResponse({
            'status': 'success',
            'subscription_id': subscription.subscription_id,
            'start_date': subscription.start_date,
            'end_date': subscription.end_date,
            'transaction_id': subscription.transaction_id
        }, status=201)