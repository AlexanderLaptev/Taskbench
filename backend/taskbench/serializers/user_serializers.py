from datetime import datetime

from rest_framework.exceptions import ValidationError
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
        token = data['token']

        if not token:
            raise ValidationError('Token not provided.')

        try:
            decoded = UntypedToken(token)
            if datetime.fromtimestamp(decoded.payload.get('exp')) < datetime.now():
                raise ValidationError('Token has expired.')
            user_id = decoded.payload.get(settings.SIMPLE_JWT['USER_ID_CLAIM'])
            user = User.objects.get(user_id=user_id)
        except:
            raise ValidationError('Invalid token or user.')
        data['user'] = user
        return data
