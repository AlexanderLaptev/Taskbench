import json
import uuid
from datetime import timedelta

from django.http import JsonResponse
from django.utils import timezone
from pydantic import ValidationError
from rest_framework.response import Response
from rest_framework.views import APIView

from ..models.models import Subscription
from ..serializers.user_serializers import JwtSerializer, user_response
from ..services.user_service import get_token, register_user, login_user, token_refresh, delete_user, change_password, \
    AuthenticationError


class RegisterView(APIView):

    def post(self, request, *args, **kwargs):
        data = json.loads(request.body)
        try:
            return user_response(*register_user(data), status=201)
        except Exception as e:
            return JsonResponse(str(e), status=400)


class LoginView(APIView):

    def post(self, request, *args, **kwargs):
        data = json.loads(request.body)
        try:
            return user_response(*login_user(data), status=200)
        except Exception as e:
            return JsonResponse(str(e), status=400)


class DeleteUserView(APIView):

    def delete(self, request, *args, **kwargs):
        token = get_token(request)
        try:
            delete_user(token)
            return Response(status=204)
        except AuthenticationError as e:
            return JsonResponse(str(e), status=401)


class ChangePasswordView(APIView):

    def patch(self, request, *args, **kwargs):
        data = json.loads(request.body)
        try:
            change_password(token=get_token(request), data=data)
            return Response(status=204)
        except AuthenticationError as e:
            return JsonResponse(str(e), status=401)
        except ValidationError as e:
            return JsonResponse(str(e), status=400)


class TokenRefreshView(APIView):

    def post(self, request, *args, **kwargs):
        data = json.loads(request.body)
        token = {'token': str(data['refresh'])}
        if data['refresh'] is None:
            return Response('No token provided.', status=400)
        try:
            return user_response(*token_refresh(token), status=200)
        except Exception as e:
            return JsonResponse(str(e), status=400)


class SubscriptionStatusView(APIView):
    def get(self, request):
        token = get_token(request)
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
        token = get_token(request)
        serializer = JwtSerializer(data=token)
        if not serializer.is_valid():
            return JsonResponse({'error': 'Invalid token'}, status=401)
        user = serializer.validated_data['user']

        now = timezone.now()
        end_date = now + timedelta(days=30)

        if user.subscriptions.filter(is_active=True, end_date__gte=now).exists():
            return JsonResponse({'error': 'User already has active subscription'}, status=400)

        subscription = Subscription.objects.create(
            user=user,
            start_date=now,
            end_date=end_date,
            is_active=True,
            transaction_id=str(uuid.uuid4())
        )

        return JsonResponse({
            'status': 'success',
            'subscription_id': subscription.subscription_id,
            'start_date': subscription.start_date,
            'end_date': subscription.end_date,
            'transaction_id': subscription.transaction_id
        }, status=201)
