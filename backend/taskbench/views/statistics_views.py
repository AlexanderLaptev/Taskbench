from django.http import JsonResponse
from rest_framework.exceptions import ValidationError
from rest_framework.views import APIView

from taskbench.serializers.statistics_serializers import statistics_response
from taskbench.services.statistics_service import get_statistics
from taskbench.services.user_service import get_token, AuthenticationError


class StatisticsView(APIView):
    """
    GET /statistics
    Возвращает статистику продуктивности для аутентифицированного пользователя:
    - done_today: количество задач, выполненных сегодня
    - max_done: максимальное количество задач, выполненных за один день
    - weekly: массив из 7 значений продуктивности (0.0-1.0) с понедельника по текущий день.
    """
    def get(self, request, *args, **kwargs):
        try:
            token = get_token(request)
            statistics = get_statistics(token)
            return statistics_response(statistics)
        except AuthenticationError as e:
            # logger.error(f"Authentication error: {str(e)}")
            return JsonResponse({'error': str(e)}, status=401)
        except ValidationError as e:
            # logger.error(f"Validation error: {str(e)}")
            return JsonResponse({'error': str(e)}, status=400)
        except Exception as e:
            # logger.error(f"Unexpected error in StatisticsView: {str(e)}", exc_info=True)
            return JsonResponse({'error': str(e)}, status=500)