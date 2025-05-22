from django.utils.dateparse import parse_datetime
from rest_framework.exceptions import ValidationError
from django.utils import timezone

from taskbench.models.models import Task, Category, TaskCategory, Subtask
from taskbench.serializers.task_serializers import TaskSearchParametersSerializer, Sort
from taskbench.services.user_service import get_user
from taskbench.utils.exceptions import NotFound


def get_task(user, task_id) -> Task:
    try:
        return Task.objects.get(task_id=task_id, user=user)
    except Task.DoesNotExist:
        raise NotFound("Task does not exist")


def get_category(user, category_id):
    try:
        return Category.objects.get(category_id=category_id, user=user)
    except Category.DoesNotExist:
        raise NotFound('Category not found or access denied')


def get_task_list(token, params):
    params_serializer = TaskSearchParametersSerializer(data=params)

    if not params_serializer.is_valid():
        raise ValidationError('Invalid params')

    user = get_user(token)
    params = params_serializer.validated_data

    sort_by = params['sort_by']
    limit = params['limit']
    offset = params['offset']
    filters = {
        'user': user,
        'is_completed': False,
        'task_categories__category_id': params['category_id'],
        'deadline__gte': params['after'],
        'deadline__lte': params['before'],
        'deadline__date': params['date']
    }
    filters = {k: v for k, v in filters.items() if v is not None}

    tasks = Task.objects.filter(**filters).prefetch_related('subtasks', 'task_categories__category')

    if sort_by == Sort.PRIORITY or sort_by == Sort.DEFAULT:
        tasks = tasks.order_by('-priority', 'deadline', 'task_id')
    elif sort_by == Sort.DEADLINE:
        tasks = tasks.order_by('deadline', '-priority', 'task_id')

    tasks = tasks[offset:offset + limit]

    return tasks


def create_task(token, data):
    user = get_user(token)
    content = data.get('content')
    dpc = data.get('dpc', {})
    subtasks = data.get('subtasks', [])

    if not content:
        raise ValidationError('Missing required field: content')

    task = Task.objects.create(
        title=content,
        deadline=parse_datetime(dpc.get('deadline')) if dpc.get('deadline') else None,
        priority=dpc.get('priority', 0),
        user=user,
        is_completed=False
    )
    if 'category_id' in dpc:
        category = get_category(user=user, category_id=dpc['category_id'])
        TaskCategory.objects.create(task=task, category=category)

    for subtask_data in subtasks:
        Subtask.objects.create(
            text=subtask_data['content'],
            task=task,
            is_completed=False
        )

    return task


def complete_task(token, task_id):
    user = get_user(token)
    task = get_task(user=user, task_id=task_id)
    if task.is_completed:
        raise ValidationError('Task already completed')
    task.is_completed = True
    task.completed_at = timezone.now()  # Устанавливаем текущую дату и время
    task.save()
    return task


def update_task(token, task_id, data):
    user = get_user(token)
    task = get_task(user=user, task_id=task_id)

    if task is None:
        raise NotFound("Task does not exist")

    content = data.get('content')
    dpc = data.get('dpc', {})

    if content:
        task.title = content
    if not dpc:
        task.save()
        return task

    if 'deadline' in dpc:
        task.deadline = parse_datetime(dpc['deadline']) if dpc['deadline'] else None
    if 'priority' in dpc:
        try:
            task.priority = int(dpc['priority'])
        except:
            raise ValidationError('Invalid priority')
    if 'category_id' in dpc:
        try:
            category = get_category(user=user, category_id=dpc['category_id'])
            task.task_categories.all().delete()  # предыдущие категории
            # TaskCategory.objects.filter(task=task, category=category).delete()
            TaskCategory.objects.create(task=task, category=category)
        except NotFound:
            raise ValidationError('Category not found or access denied')
    task.save()
    return task
