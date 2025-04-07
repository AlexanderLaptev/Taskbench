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
from rest_framework.routers import DefaultRouter
from taskbench.views import UserViewSet, TaskViewSet, SubtaskViewSet, CategoryViewSet, TaskCategoryViewSet

#НАЧАЛЬНЫЙ ВАРИАНТ

router = DefaultRouter()
router.register(r'users', UserViewSet)
router.register(r'tasks', TaskViewSet)
router.register(r'subtasks', SubtaskViewSet)
router.register(r'categories', CategoryViewSet)
router.register(r'taskcategories', TaskCategoryViewSet)

urlpatterns = [
    path('admin/', admin.site.urls),  # Админка
    path('api/', include(router.urls)),  # Добавляем 'api/' перед всеми маршрутами
]


# urlpatterns = [
#     path('admin/', admin.site.urls),
# ] - было
