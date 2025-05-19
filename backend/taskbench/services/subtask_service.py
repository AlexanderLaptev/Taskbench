from rest_framework.exceptions import ValidationError

from taskbench.models.models import Subtask
from taskbench.services.task_service import get_task
from taskbench.services.user_service import get_user
from taskbench.utils.exceptions import NotFound


def get_subtask(subtask_id, user):
    try:
        return Subtask.objects.get(subtask_id=subtask_id, task__user=user)
    except Subtask.DoesNotExist:
        raise NotFound("Subtask not found")

def create_subtask(token, task_id, data):
    user = get_user(token)
    task = get_task(user=user, task_id=task_id)

    content = data.get('content')
    is_done = data.get('is_done', False)

    if not content:
        raise ValidationError('Content cannot be empty')

    return Subtask.objects.create(
        text=content,
        task=task,
        is_completed=is_done
    )

def update_subtask(subtask_id, token, data):
    user = get_user(token)
    subtask = get_subtask(subtask_id, user)

    if 'content' in data:
        subtask.text = data['content']
    if 'is_done' in data:
        subtask.is_completed = data['is_done']

    subtask.save()
    return subtask

def delete_subtask(token, subtask_id):
    user = get_user(token)
    subtask = get_subtask(subtask_id, user)
    subtask.delete()