import json

from django.http import JsonResponse
from rest_framework.views import APIView

from suggestion.service import suggest
from taskbench.services.user_service import get_token


class SuggestionView(APIView):

    def post(self, request):
        data = json.loads(request.body)
        token = get_token(request)

        subtasks, category_name, category_id, deadline = suggest(token, data)

        return JsonResponse({
            "suggested_dpc": {
                "deadline": deadline.replace(tzinfo=None).isoformat(timespec='seconds') if deadline is not None else '',
                "priority": 0,
                "category_id": category_id if category_name is not None else '',
                "category_name": category_name if category_name is not None else '',
            },
            "suggestions": subtasks if subtasks is not None else [],
        })
