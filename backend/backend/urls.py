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
from django.urls import path, include

from taskbench.views.statisctics_views import StatisticsView
from taskbench.views.suggestion_views import SuggestionView
from taskbench.views.task_views import (
    TaskListView,
    TaskDetailView,
    SubtaskCreateView,
    SubtaskDetailView,
    CategoryListView
)
from taskbench.views.user_views import (
    RegisterView,
    LoginView,
    DeleteUserView,
    TokenRefreshView,
    ChangePasswordView,
    SubscriptionStatusView,
    CreateSubscriptionView
)

urlpatterns = [
    path("", include("dashboard.urls")),
    path('tasks/', TaskListView.as_view(), name='task_list'),
    path('tasks/<int:task_id>/', TaskDetailView.as_view(), name='task_detail'),
    path('subtasks/', SubtaskCreateView.as_view(), name='subtask_create'),
    path('subtasks/<int:subtask_id>/', SubtaskDetailView.as_view(), name='subtask_detail'),
    path('categories/', CategoryListView.as_view(), name='categories'),
    path('user/register/', RegisterView.as_view(), name='register'), # POST - создание пользователя
    path('user/login/', LoginView.as_view(), name='login'), # POST - валидация пользователя, возвращение jwt
    path('user/delete/', DeleteUserView.as_view(), name='delete_user'), # POST - валидация пользователя, возвращение jwt
    path('user/password/', ChangePasswordView.as_view(), name='change_password'),
    path('token/refresh/', TokenRefreshView.as_view(), name="token_refresh"),
    path('statistics/', StatisticsView.as_view(), name='statistics'),
    path('ai/suggestions/', SuggestionView.as_view(), name="ai_suggestions"),
    path('user/subscription/status/', SubscriptionStatusView.as_view()),
    path('user/subscription/', CreateSubscriptionView.as_view()),
]

