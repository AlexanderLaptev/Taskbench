import json

from django.http import JsonResponse, HttpResponse
from rest_framework.exceptions import ValidationError
from rest_framework.views import APIView

from ..models.models import Task, Subtask, Category
from ..serializers.task_serializers import task_list_response, task_response
from ..serializers.user_serializers import JwtSerializer
from ..services.task_service import get_task_list, create_task, complete_task, update_task
from ..services.user_service import get_token, AuthenticationError


# /tasks - GET, POST
# пример: GET http://127.0.0.1:8000/tasks/
# GET http://127.0.0.1:8000/tasks/?sort_by=priority
# GET http://127.0.0.1:8000/tasks/?sort_by=deadline
# GET http://127.0.0.1:8000/tasks/?date=2026-05-25  - с фильтром по дате
# GET http://127.0.0.1:8000/tasks/?after=2025-01-01T00:00:00Z
# GET http://127.0.0.1:8000/tasks/?before=2025-12-31T23:59:59Z
# GET http://127.0.0.1:8000/tasks/?after=2025-01-01T00:00:00Z&before=2025-12-31T23:59:59Z
# GET http://127.0.0.1:8000/tasks/?date=2025-05-01
# POST http://127.0.0.1:8000/tasks/
# {
#     "content": "Подготовить презентацию",
#     "dpc": {
#         "deadline": "2025-05-25T14:00:00Z",
#         "priority": 2,
#         "category_id": 3
#     },
#     "subtasks": [
#         {
#             "content": "Собрать материалы"
#         },
#         {
#             "content": "Создать черновик"
#         }
#     ]
# }
class TaskListView(APIView):
    def get(self, request, *args, **kwargs):
        token = get_token(request)
        params = request.GET
        try:
            return task_list_response(get_task_list(token=token, params=params))
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)

    def post(self, request, *args, **kwargs):
        token = get_token(request)
        data = json.loads(request.body)

        try:
            return task_response(create_task(token=token, data=data), 201)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)


# /tasks/{task_id} - PATCH, DELETE
# пример:DELETE http://127.0.0.1:8000/tasks/2/
# PATCH http://127.0.0.1:8000/tasks/1/
# {
#   "content": "Подготовить отчетфыфыфыф",
#   "dpc": {
#     "deadline": "2025-04-30T18:00:00Z",
#     "priority": 3,
#     "category_id": 5
#   }
# }
class TaskDetailView(APIView):


    def delete(self, request, task_id, *args, **kwargs):
        token = get_token(request)
        try:
            return task_response(complete_task(token=token,task_id=task_id), 200)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)

    def patch(self, request, task_id, *args, **kwargs):
        token = get_token(request)
        data = json.loads(request.body)
        try:
            return task_response(update_task(token=token, task_id=task_id, data=data), 200)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)
        # except Exception as e:
        #     return JsonResponse({'error': str(e)}, status=500)


# /subtasks - POST
# http://127.0.0.1:8000/subtasks/?task_id=3
# {
#   "content": "Новая подзадача2",
#   "is_done": false
# }

class SubtaskCreateView(APIView):
    def post(self, request, *args, **kwargs):
        try:
            # Get token from request and validate user
            token = get_token(request)
            serializer = JwtSerializer(data=token)
            if not serializer.is_valid():
                return JsonResponse({'error': 'Invalid token'}, status=401)
            user = serializer.validated_data['user']

            # Get task_id from request parameters
            task_id = request.GET.get('task_id')
            if not task_id:
                return JsonResponse({'error': 'task_id parameter is required'}, status=400)

            # Check if task exists and belongs to user
            try:
                task = Task.objects.get(task_id=task_id, user=user)
            except Task.DoesNotExist:
                return JsonResponse({'error': 'Task not found'}, status=404)

            # Parse request body
            data = json.loads(request.body)
            content = data.get('content')
            is_done = data.get('is_done', False)

            if not content:
                return JsonResponse({'error': 'content is required'}, status=400)

            # Create subtask
            subtask = Subtask.objects.create(
                text=content,
                task=task,
                is_completed=is_done
            )

            # Prepare response
            response_data = {
                "id": subtask.subtask_id,
                "content": subtask.text,
                "is_done": subtask.is_completed
            }

            return JsonResponse(response_data, status=201)

        except json.JSONDecodeError:
            return JsonResponse({'error': 'Invalid JSON'}, status=400)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)


# PATCH (/subtasks/{subtask_id})
# http://127.0.0.1:8000/subtasks/4/
# {
#   "content": "Обновленный текст подзадачи",
#   "is_done": true
# }
# DELETE
# http://127.0.0.1:8000/subtasks/4/
# /subtasks/{subtask_id}
class SubtaskDetailView(APIView):
    def get_subtask(self, subtask_id, user):
        try:
            return Subtask.objects.get(subtask_id=subtask_id, task__user=user)
        except Subtask.DoesNotExist:
            return None

    def patch(self, request, subtask_id, *args, **kwargs):
        try:
            # Get token from request and validate user
            token = get_token(request)
            serializer = JwtSerializer(data=token)
            if not serializer.is_valid():
                return JsonResponse({'error': 'Invalid token'}, status=401)
            user = serializer.validated_data['user']

            # Get subtask
            subtask = self.get_subtask(subtask_id, user)
            if not subtask:
                return JsonResponse({'error': 'Subtask not found'}, status=404)

            # Parse request body
            data = json.loads(request.body)

            # Update text if present in request
            if 'content' in data:
                subtask.text = data['content']

            # Update status if present in request
            if 'is_done' in data:
                subtask.is_completed = data['is_done']

            subtask.save()

            # Prepare response
            response_data = {
                "id": subtask.subtask_id,
                "content": subtask.text,
                "is_done": subtask.is_completed
            }

            return JsonResponse(response_data)

        except json.JSONDecodeError:
            return JsonResponse({'error': 'Invalid JSON'}, status=400)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)

    def delete(self, request, subtask_id, *args, **kwargs):
        try:
            # Get token from request and validate user
            token = get_token(request)
            serializer = JwtSerializer(data=token)
            if not serializer.is_valid():
                return JsonResponse({'error': 'Invalid token'}, status=401)
            user = serializer.validated_data['user']

            # Get and delete subtask
            subtask = self.get_subtask(subtask_id, user)
            if not subtask:
                return JsonResponse({'error': 'Subtask not found'}, status=404)

            subtask.delete()
            return HttpResponse(status=204)

        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)


# GET
# http://127.0.0.1:8000/categories/
# POST
# {
#     "name": "Хехе"
# }
class CategoryListView(APIView):
    def get(self, request, *args, **kwargs):
        try:
            # Аутентификация
            token = get_token(request)
            serializer = JwtSerializer(data=token)
            if not serializer.is_valid():
                return JsonResponse({'error': 'Invalid token'}, status=401)
            user = serializer.validated_data['user']

            categories = Category.objects.filter(user=user)

            data = [{
                "id": category.category_id,
                "name": category.name
            } for category in categories]

            return JsonResponse(data, safe=False)

        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)

    def post(self, request, *args, **kwargs):
        try:
            token = get_token(request)
            serializer = JwtSerializer(data=token)
            if not serializer.is_valid():
                return JsonResponse({'error': 'Invalid token'}, status=401)
            user = serializer.validated_data['user']

            data = json.loads(request.body)
            category_name = data.get('name')

            if not category_name:
                return JsonResponse({'error': 'Name is required'}, status=400)

            if len(category_name) > 50:
                return JsonResponse({'error': 'Category name too long (max 50 chars)'}, status=400)

            if Category.objects.filter(user=user, name=category_name).exists():
                return JsonResponse({'error': 'Category already exists'}, status=409)

            # Создание категории
            category = Category.objects.create(
                name=category_name,
                user=user
            )

            return JsonResponse({
                "id": category.category_id,
                "name": category.name
            }, status=201)

        except json.JSONDecodeError:
            return JsonResponse({'error': 'Invalid JSON'}, status=400)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)
