from django.utils import timezone
from datetime import datetime, timedelta
from django.db.models import Count
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from rest_framework_simplejwt.authentication import JWTAuthentication

from ..models.models import Task
from ..serializers.statistics_serializer import StatisticsSerializer


class StatisticsView(APIView):
    """
    GET /statistics

    Возвращает статистику продуктивности для аутентифицированного пользователя:
    - done_today: количество задач, выполненных сегодня
    - max_done: максимальное количество задач, выполненных за один день
    - weekly: массив из 7 значений продуктивности (0.0-1.0) по дням недели
              от воскресенья (0) до субботы (6)

    Продуктивность рассчитывается как: количество выполненных задач за день
    разделенное на максимальное количество выполненных задач за день в текущей неделе.
    """
    authentication_classes = [JWTAuthentication]
    permission_classes = [IsAuthenticated]

    def get(self, request, *args, **kwargs):
        user = request.user
        today = timezone.localdate()

        # Получаем начало недели (воскресенье)
        # 6 = суббота, 0 = понедельник в Django
        start_of_week = today - timedelta(days=(today.weekday() + 1) % 7)

        # Получаем все выполненные задачи для недели одним запросом
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

        # Преобразуем результат в словарь для быстрого поиска
        count_by_date = {
            item['completed_at__date']: item['count']
            for item in tasks_by_day
        }

        # Количество задач по дням недели
        daily_counts = []
        for i in range(7):
            day = start_of_week + timedelta(days=i)
            count = count_by_date.get(day, 0)
            daily_counts.append(count)

        # Рассчитываем статистику
        max_done = max(daily_counts) if daily_counts else 0

        # Для задач, выполненных сегодня
        today_index = (today.weekday() + 1) % 7  # Преобразуем в формат 0=воскресенье
        done_today = daily_counts[today_index]

        # Рассчитываем продуктивность по дням
        weekly = [
            count / max_done if max_done > 0 else 0.0
            for count in daily_counts
        ]

        # Сериализуем и возвращаем ответ
        serializer = StatisticsSerializer({
            'done_today': done_today,
            'max_done': max_done,
            'weekly': weekly
        })

        return Response(serializer.data)