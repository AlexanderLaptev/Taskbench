from django.http import JsonResponse


def subtask_json(subtask):
    return {
        "id": subtask.subtask_id,
        "content": subtask.text,
        "is_done": subtask.is_completed
    }


def subtask_response(subtask, status):
    return JsonResponse(subtask_json(subtask), status=status)
