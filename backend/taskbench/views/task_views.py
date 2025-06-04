import json

from django.http import JsonResponse
from rest_framework.exceptions import ValidationError
from rest_framework.views import APIView

from ..serializers.task_serializers import task_list_response, task_response
from ..services.task_service import get_task_list, create_task, complete_task, update_task
from ..services.user_service import get_token, AuthenticationError


class TaskListView(APIView):
    """
    /tasks - GET, POST
    """
    def get(self, request, *args, **kwargs):
        """
        GET http://127.0.0.1:8000/tasks/
        GET http://127.0.0.1:8000/tasks/?sort_by=priority
        GET http://127.0.0.1:8000/tasks/?sort_by=deadline
        GET http://127.0.0.1:8000/tasks/?date=2026-05-25  - с фильтром по дате
        GET http://127.0.0.1:8000/tasks/?after=2025-01-01T00:00:00Z
        GET http://127.0.0.1:8000/tasks/?before=2025-12-31T23:59:59Z
        GET http://127.0.0.1:8000/tasks/?after=2025-01-01T00:00:00Z&before=2025-12-31T23:59:59Z
        GET http://127.0.0.1:8000/tasks/?date=2025-05-01
        """
        try:
            token = get_token(request)
            params = request.GET
            return task_list_response(get_task_list(token=token, params=params))
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)

    def post(self, request, *args, **kwargs):
        """
        POST http://127.0.0.1:8000/tasks/
        {
            "content": "Подготовить презентацию",
            "dpc": { "deadline": "2025-05-25T14:00:00Z", "priority": 2, "category_id": 3 },
            "subtasks": [{ "content": "Собрать материалы" }, { "content": "Создать черновик" }]
        }
        """
        try:
            token = get_token(request)
            data = json.loads(request.body)
            return task_response(create_task(token=token, data=data), 201)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)
        except json.JSONDecodeError:
            return JsonResponse({'error': 'Invalid JSON'}, status=400)

class TaskDetailView(APIView):
    """
    /tasks/{task_id} - PATCH, DELETE
    """
    def delete(self, request, task_id, *args, **kwargs):
        """
        DELETE http://127.0.0.1:8000/tasks/2/
        """
        try:
            token = get_token(request)
            return task_response(complete_task(token=token, task_id=task_id), 200)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)

    def patch(self, request, task_id, *args, **kwargs):
        """
        PATCH http://127.0.0.1:8000/tasks/1/
        {
          "content": "Подготовить отчетфыфыфыф",
          "dpc": { "deadline": "2025-04-30T18:00:00Z", "priority": 3, "category_id": 5 }
        }
        """
        try:
            token = get_token(request)
            data = json.loads(request.body)
            return task_response(update_task(token=token, task_id=task_id, data=data), 200)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)
        except json.JSONDecodeError:
            return JsonResponse({'error': 'Invalid JSON'}, status=400)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)
