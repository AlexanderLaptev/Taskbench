from rest_framework.exceptions import ValidationError

from taskbench.models.models import Category, TaskCategory
from taskbench.serializers.category_serializers import CategorySerializer
from taskbench.services.user_service import get_user
from taskbench.utils.exceptions import AlreadyExists
from taskbench.utils.exceptions import NotFound, AuthenticationError



def get_category_list(token):
    user = get_user(token)
    return Category.objects.filter(user=user)


def get_category(user, category_id):
    try:
        return Category.objects.get(category_id=category_id, user=user)
    except Category.DoesNotExist:
        raise NotFound("Category not found or access denied")


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


def update_category(token, category_id, data):
    user = get_user(token)
    category = get_category(user, category_id)  # Проверяем существование и доступ
    serializer = CategorySerializer(data=data)
    if serializer.is_valid():
        category_name = serializer.validated_data["name"]
        if Category.objects.filter(name=category_name, user=user).exclude(category_id=category_id).exists():
            raise AlreadyExists("Category with this name already exists")
        category.name = category_name
        category.save()
        return category
    else:
        raise ValidationError(serializer.errors)


def delete_category(token, category_id):
    user = get_user(token)
    category = get_category(user, category_id)  # Проверяем существование и доступ
    # Удаляем все связи в TaskCategory, но не таски
    TaskCategory.objects.filter(category=category).delete()
    category.delete()
    return {"message": "Category deleted successfully"}

