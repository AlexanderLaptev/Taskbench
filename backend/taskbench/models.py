#cd .\backend\
#python manage.py makemigrations
#python manage.py migrate

# Создать дамп с таблицами и данными:  pg_dump -U postgres -d taskbenchDB -f bd.sql
# Этот файл можно положить в репозиторий, и другие смогут его использовать для восстановления базы.



#На другом компьютере можно выполнить:
#psql -U postgres -d taskbenchDB -f bd.sql
#Это создаст все таблицы и загрузит данные.



from django.db import models
from django.utils import timezone

class User(models.Model):
    user_id = models.AutoField(primary_key=True)
    username = models.CharField(max_length=50, unique=True, null=False)
    email = models.EmailField(max_length=100, unique=True, null=False)
    password_hash = models.CharField(max_length=255, null=False)
    created_at = models.DateTimeField(default=timezone.now)

    def __str__(self):
        return self.username


class Task(models.Model):
    task_id = models.AutoField(primary_key=True)
    title = models.CharField(max_length=255, null=False)
    deadline = models.DateTimeField(null=False)
    priority = models.BooleanField(default=False)
    status = models.CharField(max_length=20, default='active')
    created_at = models.DateTimeField(default=timezone.now)
    ai_processed = models.BooleanField(default=False)
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, blank=False, related_name='tasks') #многие к одному к юзеру

    def __str__(self):
        return self.title


class Subtask(models.Model):
    subtask_id = models.AutoField(primary_key=True)
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

