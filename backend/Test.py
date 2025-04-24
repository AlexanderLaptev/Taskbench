# backend/populate_db.py

import os
import django
import random
from datetime import timedelta
from django.utils import timezone

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "backend.settings")
django.setup()

from taskbench.models import User, Task, Subtask, Category, TaskCategory, Subscription

# Очистим все старые данные (если нужно)
TaskCategory.objects.all().delete()
Subtask.objects.all().delete()
Task.objects.all().delete()
Category.objects.all().delete()
Subscription.objects.all().delete()
User.objects.all().delete()

# Создание пользователей
users = []
for i in range(10):
    user = User.objects.create(
        username=f"user{i}",
        email=f"user{i}@example.com",
        password_hash="hash12345"
    )
    users.append(user)

# Создание задач для каждого пользователя
tasks = []
for user in users:
    for i in range(2):
        task = Task.objects.create(
            title=f"Task {i} for {user.username}",
            deadline=timezone.now() + timedelta(days=random.randint(1, 30)),
            priority=random.randint(1, 5),  # теперь это int
            status=random.choice(['active', 'completed', 'pending']),
            ai_processed=random.choice([True, False]),
            user=user
        )
        tasks.append(task)

# Подзадачи для каждой задачи
for task in tasks:
    for j in range(random.randint(1, 2)):
        Subtask.objects.create(
            number=j + 1,
            text=f"Subtask {j + 1} for {task.title}",
            is_completed=random.choice([True, False]),
            task=task
        )

# Категории для пользователей
categories = []
for user in users:
    for i in range(1, 3):
        category = Category.objects.create(
            name=f"Category{i} of {user.username}",
            user=user
        )
        categories.append(category)

# Связь задач с категориями
for task in tasks:
    user_categories = Category.objects.filter(user=task.user)
    chosen_category = random.choice(user_categories)
    TaskCategory.objects.get_or_create(task=task, category=chosen_category)

# Подписки для пользователей
for user in users:
    start = timezone.now()
    end = start + timedelta(days=30)
    Subscription.objects.create(
        user=user,
        start_date=start,
        end_date=end,
        is_active=True,
        transaction_id=f"txn-{random.randint(1000, 9999)}"
    )

print("База данных успешно заполнена.")
