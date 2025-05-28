from django.urls import path, include

from taskbench.views.category_views import CategoryListView, CategoryDetailView
from taskbench.views.statistics_views import StatisticsView
from taskbench.views.subtask_views import (
    SubtaskCreateView,
    SubtaskDetailView
)
from taskbench.views.suggestion_views import SuggestionView
from taskbench.views.task_views import (
    TaskListView,
    TaskDetailView,
)
from taskbench.views.user_views import (
    RegisterView,
    LoginView,
    DeleteUserView,
    TokenRefreshView,
    ChangePasswordView
)


urlpatterns = [
    path("", include("dashboard.urls")),
    path("", include("subscription.urls")),
    path('tasks/', TaskListView.as_view(), name='task_list'),
    path('tasks/<int:task_id>/', TaskDetailView.as_view(), name='task_detail'),
    path('subtasks/', SubtaskCreateView.as_view(), name='subtask_create'),
    path('subtasks/<int:subtask_id>/', SubtaskDetailView.as_view(), name='subtask_detail'),
    path('categories/', CategoryListView.as_view(), name='categories'),
    path('categories/<int:category_id>/', CategoryDetailView.as_view(), name='category_detail'),
    path('user/register/', RegisterView.as_view(), name='register'), # POST - создание пользователя
    path('user/login/', LoginView.as_view(), name='login'), # POST - валидация пользователя, возвращение jwt
    path('user/delete/', DeleteUserView.as_view(), name='delete_user'), # POST - валидация пользователя, возвращение jwt
    path('user/password/', ChangePasswordView.as_view(), name='change_password'),
    path('token/refresh/', TokenRefreshView.as_view(), name="token_refresh"),
    path('statistics/', StatisticsView.as_view(), name='statistics'),
    path('ai/suggestions/', SuggestionView.as_view(), name="ai_suggestions"),
]

