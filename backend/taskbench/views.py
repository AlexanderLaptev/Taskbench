from django.shortcuts import render

# curl.exe -X GET http://127.0.0.1:8000/api/users/ - в командной строке для проверки

#НАЧАЛЬНЫЙ ВАРИАНТ
from rest_framework import viewsets
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework.filters import SearchFilter, OrderingFilter
from .models import User, Task, Subtask, Category, TaskCategory
from .serializers import UserSerializer, TaskSerializer, SubtaskSerializer, CategorySerializer, TaskCategorySerializer


class UserViewSet(viewsets.ModelViewSet):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    filter_backends = [DjangoFilterBackend, SearchFilter, OrderingFilter]
    filterset_fields = '__all__'
    search_fields = ['username', 'email']
    ordering_fields = '__all__'
    ordering = ['user_id']


class TaskViewSet(viewsets.ModelViewSet):
    queryset = Task.objects.all()
    serializer_class = TaskSerializer
    filter_backends = [DjangoFilterBackend, SearchFilter, OrderingFilter]
    filterset_fields = '__all__'
    search_fields = ['title', 'status']
    ordering_fields = '__all__'
    ordering = ['task_id']


class SubtaskViewSet(viewsets.ModelViewSet):
    queryset = Subtask.objects.all()
    serializer_class = SubtaskSerializer
    filter_backends = [DjangoFilterBackend, SearchFilter, OrderingFilter]
    filterset_fields = '__all__'
    search_fields = ['text']
    ordering_fields = '__all__'
    ordering = ['subtask_id']


class CategoryViewSet(viewsets.ModelViewSet):
    queryset = Category.objects.all()
    serializer_class = CategorySerializer
    filter_backends = [DjangoFilterBackend, SearchFilter, OrderingFilter]
    filterset_fields = '__all__'
    search_fields = ['name']
    ordering_fields = '__all__'
    ordering = ['category_id']


class TaskCategoryViewSet(viewsets.ModelViewSet):
    queryset = TaskCategory.objects.all()
    serializer_class = TaskCategorySerializer
    filter_backends = [DjangoFilterBackend, SearchFilter, OrderingFilter]
    filterset_fields = '__all__'
    ordering_fields = '__all__'
    ordering = ['taskcategory_id']
