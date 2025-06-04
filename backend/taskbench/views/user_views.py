import json

from django.http import JsonResponse
from pydantic import ValidationError
from rest_framework.response import Response
from rest_framework.views import APIView

from ..serializers.user_serializers import user_response
from ..services.user_service import get_token, register_user, login_user, token_refresh, delete_user, change_password, \
    AuthenticationError


class RegisterView(APIView):

    def post(self, request, *args, **kwargs):
        try:
            data = json.loads(request.body)
            return user_response(*register_user(data), status=201)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=400)


class LoginView(APIView):

    def post(self, request, *args, **kwargs):
        try:
            data = json.loads(request.body)
            return user_response(*login_user(data), status=200)
        except Exception as e:
            print(str(e))
            return JsonResponse({'error': str(e)}, status=400)


class DeleteUserView(APIView):

    def delete(self, request, *args, **kwargs):
        token = get_token(request)
        try:
            delete_user(token)
            return Response(status=204)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)


class ChangePasswordView(APIView):

    def patch(self, request, *args, **kwargs):
        try:
            data = json.loads(request.body)
            change_password(token=get_token(request), data=data)
            return Response(status=204)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)


class TokenRefreshView(APIView):

    def post(self, request, *args, **kwargs):
        try:
            data = json.loads(request.body)
            token = {'token': str(data['refresh'])}
            if data['refresh'] is None:
                return Response('No token provided.', status=400)
            return user_response(*token_refresh(token), status=200)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=400)
