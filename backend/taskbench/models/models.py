from django.db import models
from django.utils import timezone
from django.contrib.auth.hashers import make_password, check_password

class User(models.Model):
    user_id = models.AutoField(primary_key=True)
    # username = models.CharField(max_length=50, unique=True, null=False)
    email = models.EmailField(max_length=100, unique=True, null=False)
    password_hash = models.CharField(max_length=255, null=False)
    created_at = models.DateTimeField(default=timezone.now)
    access_at = models.DateTimeField(default=timezone.now, null=True)

    def __str__(self):
        return self.email

    def set_password(self, password):
        """Хэширует пароль"""
        self.password_hash = make_password(password)

    def check_password(self, password):
        """Валидирует пароль"""
        return check_password(password, self.password_hash)


class Task(models.Model):
    task_id = models.AutoField(primary_key=True)
    title = models.CharField(max_length=255, null=False)
    deadline = models.DateTimeField(null=True)
    priority = models.IntegerField(null=False, default=0)
    is_completed = models.BooleanField(default=False)
    created_at = models.DateTimeField(default=timezone.now)
    completed_at = models.DateTimeField(null=True)
    ai_processed = models.BooleanField(default=False)
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, blank=False, related_name='tasks') #многие к одному к юзеру

    def __str__(self):
        return self.title


class Subtask(models.Model):
    subtask_id = models.AutoField(primary_key=True)
    number = models.IntegerField(default=0, null=False)
    text = models.TextField(null=False)
    is_completed = models.BooleanField(default=False)
    created_at = models.DateTimeField(default=timezone.now)
    task = models.ForeignKey(Task, on_delete=models.CASCADE, null=False, blank=False, related_name='subtasks')
    #null=False — в базе данных поле task_id не может быть NULL, т.е. каждой подзадаче обязательно соответствует задача.
    #blank=False — в формах Django (например, в админке) нельзя оставить это поле пустым. Пользователь обязан выбрать связанную задачу.
    #on_delete=models.CASCADE - Если удалить Task, то все связанные Subtask тоже автоматически удалятся.

    def __str__(self):
        return f"Subtask of {self.task.title}"


class Category(models.Model):
    category_id = models.AutoField(primary_key=True)
    name = models.CharField(max_length=50, null=False)
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, blank=False, related_name='categories')

    def __str__(self):
        return self.name


class TaskCategory(models.Model):
    taskcategory_id = models.AutoField(primary_key=True)
    task = models.ForeignKey(Task, on_delete=models.CASCADE, null=False, blank=False, related_name='task_categories')
    category = models.ForeignKey(Category, on_delete=models.CASCADE, null=False, blank=False, related_name='task_categories')

    class Meta: #одна и та же пара task + category не может повторяться
        unique_together = ('task', 'category')
        indexes = [ #явно создаётся индекс по полям task и category (в указанном порядке), и дается ему имя "unique_crud_category"
            models.Index(fields=['task', 'category'], name='unique_crud_category'),
        ]

    def __str__(self):
        return f"{self.task.title} - {self.category.name}"

class Subscription(models.Model):
    subscription_id = models.AutoField(primary_key=True)
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, blank=False, related_name='subscriptions')
    start_date = models.DateTimeField(default=timezone.now)
    end_date = models.DateTimeField(null=False)
    is_active = models.BooleanField(default=True)
    transaction_id = models.CharField(max_length=100, blank=True, null=True)

    def __str__(self):
        return f"Subscription for {self.user.username} from {self.start_date.date()} to {self.end_date.date()}"

