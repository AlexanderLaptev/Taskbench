from rest_framework.exceptions import ValidationError

from taskbench.models.models import Category
from taskbench.serializers.category_serializers import CategorySerializer
from taskbench.services.user_service import get_user
from taskbench.utils.exceptions import AlreadyExists


def get_category_list(token):
    user = get_user(token)
    return Category.objects.filter(user=user)

def create_category(token, data):
    serializer = CategorySerializer(data=data)
    user = get_user(token)
    if serializer.is_valid():
        category_name = serializer.validated_data["name"]
        if Category.objects.filter(name=category_name, user=user).exists():
            raise AlreadyExists("Category already exists")
        category = Category.objects.create(name=category_name, user=user)
        return category
    else:
        raise ValidationError(serializer.errors)

