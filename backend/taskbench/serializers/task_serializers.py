from enum import Enum

from django.http import JsonResponse
from rest_framework import serializers
from rest_framework.exceptions import ValidationError

from taskbench.serializers.subtask_serializers import subtask_json


class Sort(Enum):
    PRIORITY = 'priority'
    DEADLINE = 'deadline'
    DEFAULT = ''


def match_sort(sort_by: str | None) -> Sort:
    if sort_by is None:
        return Sort.DEFAULT
    for sort in Sort:
        if sort.value == sort_by:
            return sort
    return Sort.DEFAULT


def task_list_response(tasks):
    data = []
    for task in tasks:
        category = task.task_categories.first().category if task.task_categories.first() else None
        data.append(task_json(task, category, task.subtasks.all()))
    return JsonResponse(data, safe=False)


def task_response(task, status):
    category = task.task_categories.first().category if task.task_categories.first() else None
    return JsonResponse(task_json(task, category, task.subtasks.all()), safe=False, status=status)


def task_json(task, category, subtasks):
    return {
        "id": task.task_id,
        "content": task.title,
        "is_done": False,
        "dpc": {
            "deadline": task.deadline.replace(tzinfo=None).isoformat(
                timespec='seconds') if task.deadline is not None else None,
            "priority": task.priority,
            "category_id": category.category_id if category else 0,
            "category_name": category.name if category else ""
        },
        "subtasks": [subtask_json(subtask) for subtask in subtasks]
    }


class TaskDPCtoFlatSerializer(serializers.Serializer):
    category_id = serializers.IntegerField(required=False, allow_null=True)
    priority = serializers.IntegerField(required=False, allow_null=True)
    deadline = serializers.DateTimeField(required=False, allow_null=True)
    title = serializers.CharField()
    timestamp = serializers.DateTimeField()

    def to_internal_value(self, data):
        dpc_data = data.get('dpc', {})
        if dpc_data:
            data = {**dpc_data, **data}
            data.pop('dpc', None)

        return super().to_internal_value(data)


class TaskSearchParametersSerializer(serializers.Serializer):
    category_id = serializers.IntegerField(required=False, allow_null=True)
    sort_by = serializers.CharField(required=False, allow_null=True, allow_blank=True)
    after = serializers.DateTimeField(required=False, allow_null=True)
    before = serializers.DateTimeField(required=False, allow_null=True)
    date = serializers.DateTimeField(required=False, allow_null=True)
    offset = serializers.IntegerField(required=False, allow_null=True, default=0)
    limit = serializers.IntegerField(required=False, allow_null=True, default=10)

    def validate(self, data):
        validated_data = {'sort_by': match_sort(data.get('sort_by'))}

        date = data.get('date')
        before = data.get('before')
        after = data.get('after')

        validated_data['before'] = data.get('before')
        validated_data['after'] = data.get('after')

        if date is not None:
            if (after is not None) or (before is not None):
                raise ValidationError('Conflicting date parameters')
            validated_data['before'] = None
            validated_data['after'] = None
            validated_data['date'] = date
        else:
            validated_data['before'] = data.get('before')
            validated_data['after'] = data.get('after')
            validated_data['date'] = None
        if (before is not None) and (after is not None) and (before < after):
            raise ValidationError('Invalid date')

        validated_data['offset'] = data.get('offset') if data.get('offset') is not None else 0
        validated_data['limit'] = data.get('limit') if data.get('limit') is not None else 10
        validated_data['category_id'] = data.get('category_id')

        return validated_data
