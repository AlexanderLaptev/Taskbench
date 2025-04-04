# backend/populate_db.py

import os
import django
import random
from datetime import timedelta
from django.utils import timezone

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "backend.settings")
django.setup()

from taskbench.models import User, Task, Subtask, Category, TaskCategory

# Очистим все старые данные (если нужно)
TaskCategory.objects.all().delete()
Subtask.objects.all().delete()
Task.objects.all().delete()
Category.objects.all().delete()
User.objects.all().delete()

# Создаем пользователей
users = []
for i in range(10):
    user = User.objects.create(
        username=f"user{i}",
        email=f"user{i}@example.com",
        password_hash="hash12345"
    )
    users.append(user)

# Создаем по 2 задачи на пользователя
tasks = []
for user in users:
    for i in range(2):
        task = Task.objects.create(
            title=f"Task {i} for {user.username}",
            deadline=timezone.now() + timedelta(days=random.randint(1, 30)),
            priority=random.choice([True, False]),
            status=random.choice(['active', 'completed', 'pending']),
            ai_processed=random.choice([True, False]),
            user=user
        )
        tasks.append(task)

# Создаем по 1-2 подзадачи на каждую задачу
for task in tasks:
    for j in range(random.randint(1, 2)):
        Subtask.objects.create(
            text=f"Subtask {j} for {task.title}",
            is_completed=random.choice([True, False]),
            task=task
        )

# Создаем категории для пользователей
categories = []
for user in users:
    for i in range(1, 3):  # по 2 категории
        category = Category.objects.create(
            name=f"Category{i} of {user.username}",
            user=user
        )
        categories.append(category)

# Назначаем категории задачам
for task in tasks:
    user_categories = Category.objects.filter(user=task.user)
    chosen_category = random.choice(user_categories)
    TaskCategory.objects.get_or_create(task=task, category=chosen_category)
