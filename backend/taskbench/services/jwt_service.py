from django.http import HttpRequest
from rest_framework_simplejwt.exceptions import AuthenticationFailed, TokenError
from rest_framework_simplejwt.tokens import RefreshToken, UntypedToken
from rest_framework_simplejwt.settings import api_settings

from backend import settings
from taskbench.models.models import User
from taskbench.serializers.serializers import UserSerializer


def get_token_from_request(request: HttpRequest):
    auth_header = request.headers.get('Authorization', '')
    token = auth_header.split(' ')[1] if auth_header.startswith('Bearer ') else None
    return {'token': token}