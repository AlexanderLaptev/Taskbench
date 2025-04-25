"""
URL configuration for backend project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/5.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path
from taskbench.views.task_views import task_list, task_detail, subtask_create, subtask_detail
from taskbench.views.user_views import RegisterView, LoginView, DeleteUserView, TokenRefreshView

urlpatterns = [
    path('admin/', admin.site.urls),
    #path('api/tasks/', task_list, name='task_list'),
    path('tasks/', task_list, name='task_list'),  # Соответствует OpenAPI
    path('tasks/<int:task_id>/', task_detail, name='task_detail'), # PATCH/DELETE конкретной задачи
    path('subtasks/', subtask_create, name='subtask_create'), # POST - создание подзадачи
    path('subtasks/<int:subtask_id>/', subtask_detail, name='subtask_detail'), # PATCH/DELETE подзадачи
    path('user/register/', RegisterView.as_view(), name='register'), # POST - создание пользователя
    path('user/login', LoginView.as_view(), name='login'), # POST - валидация пользователя, возвращение jwt
    path('user/delete', DeleteUserView.as_view(), name='delete_user'), # POST - валидация пользователя, возвращение jwt
    path("token/refresh/", TokenRefreshView.as_view(), name="token_refresh"),
]

