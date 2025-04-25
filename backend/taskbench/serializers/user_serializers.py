from tokenize import TokenError

from django.contrib.auth import authenticate
from django.contrib.auth.handlers.modwsgi import check_password
from pydantic import ValidationError
from rest_framework import serializers
from rest_framework_simplejwt.tokens import UntypedToken

from backend import settings
from ..models.models import User

class UserRegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True)

    class Meta:
        model = User
        fields = ['email', 'password']

    def create(self, validated_data):
        password = validated_data.pop('password')
        user = User.objects.create(**validated_data)
        user.set_password(password)
        user.username = user.email
        user.save()
        return user

class LoginSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password = serializers.CharField(write_only=True)

    def validate(self, data):
        email = data['email']
        password = data['password']

        try:
            user = User.objects.get(email=email)
        except User.DoesNotExist:
            raise serializers.ValidationError("User with this email does not exist.")

        if not user.check_password(password):
            raise serializers.ValidationError("Invalid password.")

        data['user'] = user
        return data

class JwtSerializer(serializers.Serializer):
    token = serializers.CharField(write_only=True)

    def validate(self, data):
        token = data.get('token')
        print('VALIDATING...', token)

        if not token:
            raise ValidationError('Token not provided.')

        try:
            decoded = UntypedToken(token)
            print('DECODED...', decoded.payload)
            user_id = decoded.payload.get(settings.SIMPLE_JWT['USER_ID_CLAIM'])
            print(user_id)
            user = User.objects.get(user_id=user_id)
        except (TokenError, User.DoesNotExist):
            raise ValidationError('Invalid token or user.')
        data['user'] = user
        return data
