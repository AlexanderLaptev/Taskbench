import json

from django.http import JsonResponse
from rest_framework import status
from rest_framework.views import APIView

from taskbench.models.models import Category
from taskbench.serializers.task_serializers import TaskDPCtoFlatSerializer
from taskbench.serializers.user_serializers import JwtSerializer
from taskbench.services.jwt_service import get_token_from_request
# from taskbench.serializers.task_serializers import TaskSerializer
from taskbench.services.suggestion_service import SuggestionService



class SuggestionView(APIView):

    def post(self, request):

        data = json.loads(request.body)
        serializer = TaskDPCtoFlatSerializer(data=data)
        if not serializer.is_valid():
            return JsonResponse(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        user_serializer = JwtSerializer(data=get_token_from_request(request))
        if not user_serializer.is_valid():
            return JsonResponse(user_serializer.errors, status=status.HTTP_401_UNAUTHORIZED)
        user_id = user_serializer.validated_data['user'].user_id

        input_data = serializer.validated_data
        deadline    = input_data['deadline']
        title       = input_data['title']
        priority    = input_data['priority']
        category_id = input_data['category_id']
        timestamp   = input_data['timestamp']

        if deadline is None:
            deadline = SuggestionService(debug=True).suggest_deadline(title, now=timestamp)
        priority = SuggestionService().suggest_priority(title)

        if category_id is None:
            categories = Category.objects.filter(user_id = user_id)
            category_names = [c.name for c in categories]
            category_index = SuggestionService().suggest_category(title, category_names)
            category_name = ''
            if category_index < 0 or category_index >= len(categories):
                category_id = None
            else:
                category_id = categories[category_index].category_id
                category_name = categories[category_index].name
        else:
            category_name = Category.objects.get(category_id=category_id).name

        subtasks = SuggestionService().suggest_subtasks(title)

        return JsonResponse({
            "suggested_dpc": {
                "deadline": str(deadline) if deadline is not None else '',
                "priority": priority,
                "category_id": category_id if category_name is not None else '',
                "category_name": category_name if category_name is not None else '',
            },
            "suggestions": subtasks
        })