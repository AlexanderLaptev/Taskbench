from django.http import HttpRequest
from rest_framework.exceptions import ValidationError
from rest_framework_simplejwt.tokens import RefreshToken

from taskbench.models.models import User
from taskbench.serializers.user_serializers import JwtSerializer, UserRegisterSerializer, LoginSerializer
from taskbench.utils.exceptions import AuthenticationError


def get_token(request: HttpRequest):
    auth_header = request.headers.get('Authorization', '')
    token = auth_header.split(' ')[1] if auth_header.startswith('Bearer ') else None
    return {'token': token}


def get_user(token):
    # token = get_token(request)
    serializer = JwtSerializer(data=token)
    if serializer.is_valid():
        return serializer.validated_data['user']
    else:
        raise AuthenticationError(str(serializer.errors))


def generate_token(user):
    refresh = RefreshToken.for_user(user)
    access = refresh.access_token
    return str(refresh), str(access)


def register_user(data):
    serializer = UserRegisterSerializer(data=data)
    if serializer.is_valid():
        password = serializer.validated_data.pop('password')
        user = User(**serializer.validated_data)
        user.set_password(password)
        user.username = user.email
        user.save()
        refresh, access = generate_token(user)
        return user, refresh, access
    else:
        raise ValidationError(serializer.errors)


def login_user(data):
    serializer = LoginSerializer(data=data)
    if serializer.is_valid():
        user = serializer.validated_data['user']
        refresh, access = generate_token(user)
        return user, refresh, access
    else:
        raise ValidationError(serializer.errors)


def token_refresh(token):
    user = get_user(token)
    refresh, access = generate_token(user)
    return user, refresh, access


def delete_user(token):
    user = get_user(token)
    user.delete()


def change_password(token, data):
    user = get_user(token)
    old_password = data.get('old_password')
    new_password = data.get('new_password')
    if not old_password or not new_password:
        raise ValidationError('Both old_password and new_password are required')

    if not user.check_password(old_password):
        raise ValidationError('Password is incorrect')

    try:
        user.set_password(new_password)
        user.save()
    except Exception as e:
        raise ValidationError(str(e))
