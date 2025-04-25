from django.shortcuts import render
from django.http import JsonResponse, HttpResponseNotAllowed, HttpResponseBadRequest, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils.dateparse import parse_datetime
from ..models.models import Task, Subtask, TaskCategory
import json

# /tasks - GET, POST
#пример: GET http://127.0.0.1:8000/tasks/?user_id=1
#user_id явно в параметрах/теле запроса Это временное решение
#GET http://127.0.0.1:8000/tasks/?user_id=1&sort_by=priority
#GET http://127.0.0.1:8000/tasks/?user_id=1&sort_by=deadline
#POST http://127.0.0.1:8000/tasks/?user_id=1
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
@csrf_exempt
def task_list(request):
    if request.method == 'GET':
        try:
            # Получаем параметры запроса
            category_id = request.GET.get('category_id')
            sort_by = request.GET.get('sort_by')
            date = request.GET.get('date')
            offset = int(request.GET.get('offset', 0))
            limit = int(request.GET.get('limit', 10))
            user_id = request.GET.get('user_id')

            if not user_id:
                return JsonResponse({'error': 'user_id parameter is required'}, status=400)

            # Базовый запрос - исключаем completed задачи
            tasks = Task.objects.filter(user_id=user_id, status='active') \
                .prefetch_related('subtasks', 'task_categories__category')

            # Применяем фильтры
            if category_id:
                tasks = tasks.filter(task_categories__category_id=category_id)
            if date:
                tasks = tasks.filter(deadline__date=date)
            if sort_by in ['priority', 'deadline']:
                tasks = tasks.order_by(sort_by)

            # Пагинация
            tasks = tasks[offset:offset + limit]

            # Формируем ответ
            data = []
            for task in tasks:
                category = task.task_categories.first().category if task.task_categories.first() else None

                task_data = {
                    "id": task.task_id,
                    "content": task.title,
                    "is_done": False,  # Все задачи здесь активные, поэтому всегда False
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

    elif request.method == 'POST':
        try:
            data = json.loads(request.body)
            task_text = data.get('content')
            user_id = request.GET.get('user_id')  # Теперь берем из URL параметров
            dpc = data.get('dpc', {})
            subtasks = data.get('subtasks', [])  # Получаем список подзадач

            if not all([task_text, user_id]):
                return JsonResponse({'error': 'Missing required fields: content and user_id'}, status=400)

            # Создаем задачу
            task = Task.objects.create(
                title=task_text,
                deadline=parse_datetime(dpc.get('deadline')) if dpc.get('deadline') else None,
                priority=dpc.get('priority', 0),
                user_id=user_id,
                status='active'
            )

            # Добавляем категорию, если указана
            if 'category_id' in dpc:
                TaskCategory.objects.create(task=task, category_id=dpc['category_id'])

            # Создаем подзадачи
            for subtask_data in subtasks:
                Subtask.objects.create(
                    text=subtask_data['content'],
                    task=task,
                    is_completed=False
                )

            # Получаем обновленные данные задачи
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

@csrf_exempt
def task_detail(request, task_id):
    try:
        task = Task.objects.get(task_id=task_id)
    except Task.DoesNotExist:
        return JsonResponse({'error': 'Task not found'}, status=404)

    if request.method == 'DELETE':
        try:
            # Проверяем, не завершена ли задача уже
            if task.status == 'completed':
                return JsonResponse({'error': 'Task already completed'}, status=400)

            # Помечаем задачу как выполненную
            task.status = 'completed'
            task.save()

            # Возвращаем обновленную задачу
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

    elif request.method == 'PATCH':
        try:
            data = json.loads(request.body)
            # Обновляем только разрешенные поля задачи
            if 'content' in data:
                task.title = data['content']
            if 'dpc' in data:
                dpc = data['dpc']
                if 'deadline' in dpc:
                    task.deadline = parse_datetime(dpc['deadline']) if dpc['deadline'] else None
                if 'priority' in dpc:
                    task.priority = dpc['priority']
                # Обновляем только category_id (игнорируем category_name из запроса)
                if 'category_id' in dpc:
                    task.task_categories.all().delete()
                    if dpc['category_id']:
                        TaskCategory.objects.create(task=task, category_id=dpc['category_id'])
            task.save()
            # Формируем ответ с именем категории из БД
            category = task.task_categories.first().category if task.task_categories.first() else None
            response_data = {
                "id": task.task_id,
                "content": task.title,
                "is_done": task.status.lower() == 'completed',
                "dpc": {
                    "deadline": task.deadline.isoformat() if task.deadline else None,
                    "priority": task.priority,
                    "category_id": category.category_id if category else 0,
                    "category_name": category.name if category else ""  # Берем имя из БД
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
    else:
        return JsonResponse({'error': 'Method not allowed'}, status=405)


# /subtasks - POST
#http://127.0.0.1:8000/subtasks/?task_id=3
#{
#   "content": "Новая подзадача2",
#   "is_done": false
# }
@csrf_exempt
def subtask_create(request):
    if request.method == 'POST':
        try:
            # Получаем task_id из параметров запроса
            task_id = request.GET.get('task_id')
            if not task_id:
                return JsonResponse({'error': 'task_id parameter is required'}, status=400)

            # Проверяем существование задачи
            try:
                task = Task.objects.get(task_id=task_id)
            except Task.DoesNotExist:
                return JsonResponse({'error': 'Task not found'}, status=404)

            # Парсим тело запроса
            data = json.loads(request.body)
            content = data.get('content')
            is_done = data.get('is_done', False)  # По умолчанию False, если не указано

            if not content:
                return JsonResponse({'error': 'content is required'}, status=400)

            # Создаем подзадачу
            subtask = Subtask.objects.create(
                text=content,
                task=task,
                is_completed=is_done
            )

            # Формируем ответ
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
    else:
        return JsonResponse({'error': 'Method not allowed'}, status=405)


#PATCH (/subtasks/{subtask_id})
#http://127.0.0.1:8000/subtasks/4/
#{
#   "content": "Обновленный текст подзадачи",
#   "is_done": true
# }
#DELETE
#http://127.0.0.1:8000/subtasks/4/
#/subtasks/{subtask_id}
@csrf_exempt
def subtask_detail(request, subtask_id):
    if request.method == 'PATCH':
        try:
            # Получаем подзадачу
            try:
                subtask = Subtask.objects.get(subtask_id=subtask_id)
            except Subtask.DoesNotExist:
                return JsonResponse({'error': 'Subtask not found'}, status=404)

            # Парсим тело запроса
            data = json.loads(request.body)

            # Обновляем текст если он есть в запросе
            if 'content' in data:
                subtask.text = data['content']

            # Обновляем статус если он есть в запросе
            if 'is_done' in data:
                subtask.is_completed = data['is_done']

            subtask.save()

            # Формируем ответ
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


    elif request.method == 'DELETE':
        try:
            # Получаем и сразу удаляем подзадачу
            try:
                subtask = Subtask.objects.get(subtask_id=subtask_id)
                subtask.delete()
                return HttpResponse(status=204)  # 204 No Content - стандартный ответ для успешного удаления
            except Subtask.DoesNotExist:
                return JsonResponse({'error': 'Subtask not found'}, status=404)

        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)
    else:
        return JsonResponse({'error': 'Method not allowed'}, status=405)
