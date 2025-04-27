from django.shortcuts import render
from django.http import JsonResponse, HttpResponseNotAllowed, HttpResponseBadRequest, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils.dateparse import parse_datetime
from rest_framework.views import APIView

from ..models.models import Task, Subtask, TaskCategory, Category
import json

from ..serializers.user_serializers import JwtSerializer
from ..services.jwt_service import get_token_from_request


# /tasks - GET, POST
#пример: GET http://127.0.0.1:8000/tasks/
#GET http://127.0.0.1:8000/tasks/?sort_by=priority
#GET http://127.0.0.1:8000/tasks/?sort_by=deadline
#GET http://127.0.0.1:8000/tasks/?date=2026-05-25  - с фильтром по дате
#POST http://127.0.0.1:8000/tasks/
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
        try:
            # Get token from request and validate user
            token = get_token_from_request(request)
            serializer = JwtSerializer(data=token)
            if not serializer.is_valid():
                return JsonResponse({'error': 'Invalid token'}, status=401)
            user = serializer.validated_data['user']

            # Get request parameters
            category_id = request.GET.get('category_id')
            sort_by = request.GET.get('sort_by')
            date = request.GET.get('date')
            offset = int(request.GET.get('offset', 0))
            limit = int(request.GET.get('limit', 10))

            # Base query - exclude completed tasks
            tasks = Task.objects.filter(user=user, is_completed=False) \
                .prefetch_related('subtasks', 'task_categories__category')

            # Apply filters
            if category_id:
                tasks = tasks.filter(task_categories__category_id=category_id)
            if date:
                tasks = tasks.filter(deadline__date=date)

            # Apply sorting - primary by requested field, secondary by the other field
            if sort_by == 'priority':
                tasks = tasks.order_by('priority', 'deadline')  # Сначала по приоритету, потом по дедлайну
            elif sort_by == 'deadline':
                tasks = tasks.order_by('deadline', 'priority')  # Сначала по дедлайну, потом по приоритету
            else:
                # Default sorting if none specified
                tasks = tasks.order_by('priority', 'deadline')

            # Pagination
            tasks = tasks[offset:offset + limit]

            # Prepare response
            data = []
            for task in tasks:
                category = task.task_categories.first().category if task.task_categories.first() else None

                task_data = {
                    "id": task.task_id,
                    "content": task.title,
                    "is_done": False,
                    "dpc": {
                        "deadline": task.deadline.isoformat(),
                        "priority": task.priority,
                        "category_id": category.category_id if category else 0,
                        "category_name": category.name if category else ""
                    },
                    "subtasks": [
                        {
                            "id": subtask.subtask_id,
                            "content": subtask.text,
                            "is_done": subtask.is_completed
                        }
                        for subtask in task.subtasks.all()
                    ]
                }
                data.append(task_data)

            return JsonResponse(data, safe=False)

        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)

    def post(self, request, *args, **kwargs):
        try:
            # Get token from request and validate user
            token = get_token_from_request(request)
            serializer = JwtSerializer(data=token)
            if not serializer.is_valid():
                return JsonResponse({'error': 'Invalid token'}, status=401)
            user = serializer.validated_data['user']

            data = json.loads(request.body)
            task_text = data.get('content')
            dpc = data.get('dpc', {})
            subtasks = data.get('subtasks', [])

            if not task_text:
                return JsonResponse({'error': 'Missing required field: content'}, status=400)

            # Create task
            task = Task.objects.create(
                title=task_text,
                deadline=parse_datetime(dpc.get('deadline')) if dpc.get('deadline') else None,
                priority=dpc.get('priority', 0),
                user=user,
                is_completed=False
            )

            # Add category if specified
            if 'category_id' in dpc:
                category_id = dpc['category_id']
                try:
                    # Проверяем что категория принадлежит текущему пользователю
                    category = Category.objects.get(category_id=category_id, user=user)
                except Category.DoesNotExist:
                    return JsonResponse(
                        {'error': 'Category not found or access denied'},
                        status=400
                    )

                TaskCategory.objects.create(task=task, category=category)

            # Create subtasks
            for subtask_data in subtasks:
                Subtask.objects.create(
                    text=subtask_data['content'],
                    task=task,
                    is_completed=False
                )

            # Get updated task data
            category = task.task_categories.first().category if task.task_categories.first() else None
            subtasks_data = [{
                "id": s.subtask_id,
                "content": s.text,
                "is_done": s.is_completed
            } for s in task.subtasks.all()]

            response_data = {
                "id": task.task_id,
                "content": task.title,
                "is_done": False,
                "dpc": {
                    "deadline": task.deadline.isoformat() if task.deadline else None,
                    "priority": task.priority,
                    "category_id": category.category_id if category else 0,
                    "category_name": category.name if category else ""
                },
                "subtasks": subtasks_data
            }

            return JsonResponse(response_data, status=201)

        except json.JSONDecodeError:
            return JsonResponse({'error': 'Invalid JSON'}, status=400)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=400)



# /tasks/{task_id} - PATCH, DELETE
#пример:DELETE http://127.0.0.1:8000/tasks/2/
#PATCH http://127.0.0.1:8000/tasks/1/
# {
#   "content": "Подготовить отчетфыфыфыф",
#   "dpc": {
#     "deadline": "2025-04-30T18:00:00Z",
#     "priority": 3,
#     "category_id": 5
#   }
# }
class TaskDetailView(APIView):
    def get_task(self, task_id, user):
        try:
            return Task.objects.get(task_id=task_id, user=user)
        except Task.DoesNotExist:
            return None

    def delete(self, request, task_id, *args, **kwargs):
        # Get token from request and validate user
        token = get_token_from_request(request)
        serializer = JwtSerializer(data=token)
        if not serializer.is_valid():
            return JsonResponse({'error': 'Invalid token'}, status=401)
        user = serializer.validated_data['user']

        task = self.get_task(task_id, user)
        if not task:
            return JsonResponse({'error': 'Task not found'}, status=404)

        try:
            # Check if task is already completed
            if task.is_completed:
                return JsonResponse({'error': 'Task already completed'}, status=400)

            # Mark task as completed
            task.is_completed = True
            task.save()

            # Return updated task
            category = task.task_categories.first().category if task.task_categories.first() else None
            response_data = {
                "id": task.task_id,
                "content": task.title,
                "is_done": True,
                "dpc": {
                    "deadline": task.deadline.isoformat() if task.deadline else None,
                    "priority": task.priority,
                    "category_id": category.category_id if category else 0,
                    "category_name": category.name if category else ""
                },
                "subtasks": [
                    {
                        "id": s.subtask_id,
                        "content": s.text,
                        "is_done": s.is_completed
                    } for s in task.subtasks.all()
                ]
            }
            return JsonResponse(response_data)

        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)

    def patch(self, request, task_id, *args, **kwargs):
        # Get token from request and validate user
        token = get_token_from_request(request)
        serializer = JwtSerializer(data=token)
        if not serializer.is_valid():
            return JsonResponse({'error': 'Invalid token'}, status=401)
        user = serializer.validated_data['user']

        task = self.get_task(task_id, user)
        if not task:
            return JsonResponse({'error': 'Task not found'}, status=404)

        try:
            data = json.loads(request.body)
            # Update allowed task fields
            if 'content' in data:
                task.title = data['content']
            if 'dpc' in data:
                dpc = data['dpc']
                if 'deadline' in dpc:
                    task.deadline = parse_datetime(dpc['deadline']) if dpc['deadline'] else None
                if 'priority' in dpc:
                    task.priority = dpc['priority']
                if 'category_id' in dpc:
                    task.task_categories.all().delete()
                    if dpc['category_id']:
                        try:
                            # Проверяем, что категория принадлежит текущему пользователю
                            category = Category.objects.get(
                                category_id=dpc['category_id'],
                                user=user
                            )
                            TaskCategory.objects.create(task=task, category=category)
                        except Category.DoesNotExist:
                            return JsonResponse(
                                {'error': 'Category not found or access denied'},
                                status=400
                            )
                task.save()
            # Form response with category name from DB
            category = task.task_categories.first().category if task.task_categories.first() else None
            response_data = {
                "id": task.task_id,
                "content": task.title,
                "is_done": task.is_completed,
                "dpc": {
                    "deadline": task.deadline.isoformat() if task.deadline else None,
                    "priority": task.priority,
                    "category_id": category.category_id if category else 0,
                    "category_name": category.name if category else ""
                },
                "subtasks": [
                    {
                        "id": s.subtask_id,
                        "content": s.text,
                        "is_done": s.is_completed
                    } for s in task.subtasks.all()
                ]
            }
            return JsonResponse(response_data)
        except json.JSONDecodeError:
            return JsonResponse({'error': 'Invalid JSON'}, status=400)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=400)


# /subtasks - POST
#http://127.0.0.1:8000/subtasks/?task_id=3
#{
#   "content": "Новая подзадача2",
#   "is_done": false
# }

class SubtaskCreateView(APIView):
    def post(self, request, *args, **kwargs):
        try:
            # Get token from request and validate user
            token = get_token_from_request(request)
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


#PATCH (/subtasks/{subtask_id})
#http://127.0.0.1:8000/subtasks/4/
#{
#   "content": "Обновленный текст подзадачи",
#   "is_done": true
# }
#DELETE
#http://127.0.0.1:8000/subtasks/4/
#/subtasks/{subtask_id}
class SubtaskDetailView(APIView):
    def get_subtask(self, subtask_id, user):
        try:
            return Subtask.objects.get(subtask_id=subtask_id, task__user=user)
        except Subtask.DoesNotExist:
            return None

    def patch(self, request, subtask_id, *args, **kwargs):
        try:
            # Get token from request and validate user
            token = get_token_from_request(request)
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
            token = get_token_from_request(request)
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
            token = get_token_from_request(request)
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
            token = get_token_from_request(request)
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