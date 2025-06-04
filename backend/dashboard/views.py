from datetime import timedelta
from django.utils import timezone
from django.contrib.auth import authenticate, login
from django.contrib.auth.decorators import login_required, user_passes_test
from django.core.paginator import Paginator
from django.http import HttpResponseForbidden, JsonResponse
from django.shortcuts import render, redirect
from django.views.decorators.csrf import csrf_protect

from taskbench.models.models import Subscription, Subtask, Task, User


@csrf_protect
def custom_login(request):
    if request.method == "POST":
        username = request.POST.get("username")
        password = request.POST.get("password")
        user = authenticate(request, username=username, password=password)
        if user is not None and user.is_staff:
            login(request, user)
            return redirect("dashboard")
        else:
            return render(request, "dashboard/login.html", {"error": "Неверные данные"})

    return render(request, "dashboard/login.html")


@login_required
def dashboard_view(request):
    if not request.user.is_staff:
        return HttpResponseForbidden("Нет доступа")
    return render(request, "dashboard/dashboard.html")

@user_passes_test(lambda u: u.is_staff)
def subscription_page(request):
    return render(request, "dashboard/subscriptions.html")

@user_passes_test(lambda u: u.is_staff)
def stats_api(request):
    if request.method == "GET":
        now = timezone.now()
        today = now.date()
        start_of_week = now - timedelta(days=now.weekday())
        start_of_week = start_of_week.replace(hour=0, minute=0, second=0, microsecond=0)

        data = {
            "total_users": User.objects.all().count(),
            "total_subscribers": Subscription.objects.values('user').distinct().count(),
            "new_users_week": User.objects.filter(created_at__gte=start_of_week).count(),
            "new_users_today": User.objects.filter(created_at__date=today).count(),
            "new_subs_week": Subscription.objects.filter(start_date__gt=start_of_week).count(),
            "new_subs_today": Subscription.objects.filter(start_date__date=today).count(),
            "active_users_week": User.objects.filter(access_at__gt=start_of_week).count(),
            "active_users_today": User.objects.filter(access_at__date=today).count(),
            "total_tasks": Task.objects.all().count(),
            "tasks_week": Task.objects.filter(created_at__gte=start_of_week).count(),
            "tasks_today": Task.objects.filter(created_at__date=today).count(),
            "total_subtasks": Subtask.objects.all().count(),
        }
        return JsonResponse(data)

@user_passes_test(lambda u: u.is_staff)
def subscription_list_api(request):
    page_number = request.GET.get("page", 1)
    page_size = 10  # кол-во подписок на страницу

    subscriptions = Subscription.objects.select_related("user").order_by("-start_date")
    paginator = Paginator(subscriptions, page_size)
    page = paginator.get_page(page_number)

    data = {
        "subscriptions": [
            {
                "id": sub.subscription_id,
                "email": sub.user.email,
                "start_date": sub.start_date.strftime("%Y-%m-%d") if sub.start_date else "N/A",
                "end_date": sub.end_date.strftime("%Y-%m-%d") if sub.end_date else "N/A",
                "is_active": sub.is_active,
                "transaction_id": sub.latest_yookassa_payment_id or "-",
            }
            for sub in page.object_list
        ],
        "page": page.number,
        "total_pages": paginator.num_pages,
        "has_next": page.has_next(),
        "has_previous": page.has_previous(),
    }

    return JsonResponse(data)