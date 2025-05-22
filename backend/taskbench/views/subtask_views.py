import json
from django.http import JsonResponse, HttpResponse
from rest_framework.exceptions import ValidationError
from rest_framework.response import Response
from rest_framework.views import APIView

from taskbench.serializers.subtask_serializers import subtask_response
from taskbench.services.subtask_service import create_subtask, update_subtask, delete_subtask
from taskbench.services.user_service import get_token
from taskbench.utils.exceptions import NotFound, AuthenticationError



class SubtaskCreateView(APIView):
    def post(self, request, *args, **kwargs):
        """
        POST /subtasks
        http://127.0.0.1:8000/subtasks/?task_id=3
        { "content": "Новая подзадача2", "is_done": false }
        """
        try:
            task_id = request.GET.get('task_id')
            data = json.loads(request.body)
            if not task_id:
                return JsonResponse({'error': 'task_id parameter is required'}, status=400)
            return subtask_response(create_subtask(token=get_token(request), task_id=task_id, data=data), status=201)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)
        except json.JSONDecodeError:
            return JsonResponse({'error': 'Invalid JSON'}, status=400)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except NotFound as e:
            return JsonResponse({'error': str(e)}, status=404)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)



class SubtaskDetailView(APIView):
    def patch(self, request, subtask_id, *args, **kwargs):
        """
        PATCH /subtasks/{subtask_id}
        http://127.0.0.1:8000/subtasks/4/
        { "content": "Обновленный текст подзадачи", "is_done": true }
        """
        try:
            token = get_token(request)
            data = json.loads(request.body)
            return subtask_response(update_subtask(token=token, subtask_id=subtask_id, data=data), 200)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)
        except json.JSONDecodeError:
            return JsonResponse({'error': 'Invalid JSON'}, status=400)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except NotFound as e:
            return JsonResponse({'error': str(e)}, status=404)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)

    def delete(self, request, subtask_id, *args, **kwargs):
        """
        DELETE /subtasks/{subtask_id}
        http://127.0.0.1:8000/subtasks/4/
        """
        try:
            token = get_token(request)
            delete_subtask(token=token, subtask_id=subtask_id)
            return Response(status=204)
        except ValidationError as e:
            return JsonResponse({'error': str(e)}, status=400)
        except AuthenticationError as e:
            return JsonResponse({'error': str(e)}, status=401)
        except NotFound as e:
            return JsonResponse({'error': str(e)}, status=404)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)
