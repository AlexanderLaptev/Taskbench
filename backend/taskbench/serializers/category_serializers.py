from django.http import JsonResponse

from taskbench.models.models import Category
from rest_framework import serializers, status


def category_list_response(categories, status):
    data = [{
        "id": category.category_id,
        "name": category.name
    } for category in categories]

    return JsonResponse(data, safe=False, status=status)


def category_response(category, status):
    return JsonResponse(category_json(category), safe=False, status=status)


def category_json(category: Category):
    return {
        "id": category.category_id,
        "name": category.name
    }


class CategorySerializer(serializers.Serializer):
    name = serializers.CharField(required=True)

    def validate(self, data):
        category_name = data.get("name")
        if len(category_name) > 50:
            raise serializers.ValidationError("Category name too long (max 50 chars)")
        return {"name": category_name}
