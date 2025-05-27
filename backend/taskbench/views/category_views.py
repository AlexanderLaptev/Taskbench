from django.http import JsonResponse
from rest_framework.exceptions import ValidationError
import json
from rest_framework.views import APIView

from taskbench.serializers.category_serializers import category_list_response, category_response
from taskbench.services.category_service import get_category_list, create_category
from taskbench.services.user_service import get_token
from taskbench.utils.exceptions import AuthenticationError, AlreadyExists

from taskbench.services.category_service import update_category, delete_category
from taskbench.utils.exceptions import NotFound, AuthenticationError


class CategoryListView(APIView):
    """
    GET, POST http://127.0.0.1:8000/categories/
    """
    def get(self, request, *args, **kwargs):
        try:
            token = get_token(request)
            return category_list_response(get_category_list(token), 200)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)

    def post(self, request, *args, **kwargs):
        """
        POST
        { "name": "Хехе" }
        """
        try:
            token = get_token(request)
            data = json.loads(request.body)
            return category_response(create_category(token=token, data=data), 201)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except AlreadyExists as e:
            return JsonResponse({'error': str(e)}, status=409)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)
        except json.JSONDecodeError:
            return JsonResponse({'error': 'Invalid JSON'}, status=400)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)


class CategoryDetailView(APIView):
    """
    PATCH, DELETE http://127.0.0.1:8000/categories/<category_id>/
    """
    def patch(self, request, category_id, *args, **kwargs):
        """
        PATCH http://127.0.0.1:8000/categories/1/
        { "name": "Новое название" }
        """
        try:
            token = get_token(request)
            data = json.loads(request.body)
            updated_category = update_category(token=token, category_id=category_id, data=data)
            return category_response(updated_category, 200)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except NotFound as e:
            return JsonResponse({'error': str(e)}, status=404)
        except AlreadyExists as e:
            return JsonResponse({'error': str(e)}, status=409)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)
        except json.JSONDecodeError:
            return JsonResponse({'error': 'Invalid JSON'}, status=400)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)

    def delete(self, request, category_id, *args, **kwargs):
        """
        DELETE http://127.0.0.1:8000/categories/1/
        """
        try:
            token = get_token(request)
            result = delete_category(token=token, category_id=category_id)
            return JsonResponse(result, status=200)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except NotFound as e:
            return JsonResponse({'error': str(e)}, status=404)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)
