from django.http import JsonResponse
from rest_framework.exceptions import ValidationError
import json
from rest_framework.views import APIView

from taskbench.serializers.category_serializers import category_list_response, category_response
from taskbench.services.category_service import get_category_list, create_category
from taskbench.services.user_service import get_token
from taskbench.utils.exceptions import AuthenticationError, AlreadyExists


# GET
# http://127.0.0.1:8000/categories/
# POST
# {
#     "name": "Хехе"
# }
class CategoryListView(APIView):

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