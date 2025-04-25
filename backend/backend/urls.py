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

from django.urls import path, include
from taskbench.views.task_views import task_list, task_detail, subtask_create, subtask_detail



urlpatterns = [
    path('admin/', admin.site.urls),
    #path('api/tasks/', task_list, name='task_list'),
    path('tasks/', task_list, name='task_list'),  # Соответствует OpenAPI
    path('tasks/<int:task_id>/', task_detail, name='task_detail'), # PATCH/DELETE конкретной задачи
    path('subtasks/', subtask_create, name='subtask_create'), # POST - создание подзадачи
    path('subtasks/<int:subtask_id>/', subtask_detail, name='subtask_detail'), # PATCH/DELETE подзадачи
]


# from rest_framework.routers import DefaultRouter
# from taskbench.views import UserViewSet, TaskViewSet, SubtaskViewSet, CategoryViewSet, TaskCategoryViewSet
#
# #НАЧАЛЬНЫЙ ВАРИАНТ
#
# router = DefaultRouter()
# router.register(r'users', UserViewSet)
# router.register(r'tasks', TaskViewSet)
# router.register(r'subtasks', SubtaskViewSet)
# router.register(r'categories', CategoryViewSet)
# router.register(r'taskcategories', TaskCategoryViewSet)
#
# urlpatterns = [
#     path('admin/', admin.site.urls),  # Админка
#     path('api/', include(router.urls)),  # Добавляем 'api/' перед всеми маршрутами
# ]

