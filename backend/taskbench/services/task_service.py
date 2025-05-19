from rest_framework.exceptions import ValidationError

from taskbench.models.models import Task
from taskbench.serializers.task_serializers import TaskSearchParametersSerializer, Sort
from taskbench.serializers.user_serializers import JwtSerializer
from taskbench.services.user_service import AuthenticationError


def get_task_list(token, params):
    user_serializer = JwtSerializer(data=token)
    params_serializer = TaskSearchParametersSerializer(data=params)

    if not user_serializer.is_valid():
        raise AuthenticationError('Invalid token')
    if not params_serializer.is_valid():
        raise ValidationError('Invalid params')

    user = user_serializer.validated_data['user']
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
