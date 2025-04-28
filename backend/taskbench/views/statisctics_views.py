from django.utils import timezone
from datetime import datetime, timedelta
from django.db.models import Count
from rest_framework import status
from rest_framework.views import APIView
from rest_framework.response import Response

from ..models.models import Task
from ..serializers.statistics_serializer import StatisticsSerializer
from ..serializers.user_serializers import JwtSerializer
from ..services.jwt_service import get_token_from_request


class StatisticsView(APIView):
    """
    GET /statistics

    Возвращает статистику продуктивности для аутентифицированного пользователя:
    - done_today: количество задач, выполненных сегодня
    - max_done: максимальное количество задач, выполненных за один день
    - weekly: массив из 7 значений продуктивности (0.0-1.0) за последние семь дней.

    Продуктивность рассчитывается как: количество выполненных задач за день
    разделенное на максимальное количество выполненных задач за день в текущей неделе.
    """
    def get(self, request, *args, **kwargs):
        serializer = JwtSerializer(data=get_token_from_request(request))
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_401_UNAUTHORIZED)

        user = serializer.validated_data['user']
        today = timezone.now().date()
        start_of_week = today - timedelta(days=6) # 7 дней назад
        tasks_by_day = (
            Task.objects
            .filter(
                user=user,
                is_completed=True,
                completed_at__date__gte=start_of_week,
                completed_at__date__lte=today
            )
            .values('completed_at__date')
            .annotate(count=Count('task_id'))
        )
        count_by_date = {
            item['completed_at__date']: item['count']
            for item in tasks_by_day
        }
        daily_counts = []
        for i in range(7):
            day = start_of_week + timedelta(days=i)
            count = count_by_date.get(day, 0)
            daily_counts.append(count)

        max_done = max(daily_counts) if daily_counts else 0
        done_today = daily_counts[6]
        weekly = [
            count / max_done if max_done > 0 else 0.0
            for count in daily_counts
        ]
        serializer = StatisticsSerializer({
            'done_today': done_today,
            'max_done': max_done,
            'weekly': weekly
        })

        return Response(serializer.data)