import json

from django.http import JsonResponse
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken
from ..models.models import User

from ..serializers.user_serializers import UserRegisterSerializer, LoginSerializer, JwtSerializer
from ..services.jwt_service import get_token_from_request


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
        print(token)
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

        if not request.user.check_password(old_password):
            return JsonResponse(
                {"error": "Old password is incorrect"},
                status=400
            )

        request.user.set_password(new_password)
        request.user.save()

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