import logging
from datetime import timedelta

from django.db.models import Count
from django.utils import timezone

from taskbench.models.models import Task
from taskbench.services.user_service import get_user

logger = logging.getLogger(__name__)


def get_statistics(token):
    """
    Возвращает статистику продуктивности для пользователя:
    - done_today: количество задач, выполненных сегодня
    - max_done: максимальное количество задач за день в текущей неделе
    - weekly: массив из 7 значений (float 0.0-1.0) с понедельника по воскресенье
    """

    user = get_user(token)

    # Определяем начало текущей недели (понедельник)
    today = timezone.now().date()
    start_of_week = today - timedelta(days=today.weekday())
    logger.debug(f"Calculating statistics for user {user.email}, week starting {start_of_week}")

    # Получаем задачи за неделю
    try:
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
    except Exception as e:
        logger.error(f"Error querying tasks: {str(e)}")
        raise

    # Формируем словарь: дата -> количество задач
    count_by_date = {
        item['completed_at__date']: item['count']
        for item in tasks_by_day
    }
    logger.debug(f"Tasks by day: {count_by_date}")

    # Формируем массив из 7 дней, начиная с понедельника
    daily_counts = []
    for i in range(7):
        day = start_of_week + timedelta(days=i)
        count = count_by_date.get(day, 0)
        daily_counts.append(count)

    # Рассчитываем статистику
    max_done = max(daily_counts) if daily_counts else 0
    done_today = count_by_date.get(today, 0)
    weekly = [
        count / max_done if max_done > 0 else 0.0
        for count in daily_counts
    ]

    logger.debug(f"Statistics: done_today={done_today}, max_done={max_done}, weekly={weekly}")
    return {
        'done_today': done_today,
        'max_done': max_done,
        'weekly': weekly
    }
