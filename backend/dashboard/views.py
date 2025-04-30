from django.contrib.auth import authenticate, login
from django.contrib.auth.decorators import login_required, user_passes_test
from django.core.paginator import Paginator
from django.shortcuts import render, redirect
from django.http import HttpResponseForbidden, JsonResponse
from django.views.decorators.csrf import csrf_protect, csrf_exempt

from taskbench.models.models import Subscription


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
    data = {
        "total_users": 280,
        "total_subscribers": 28,
        "new_users_week": 40,
        "new_users_today": 10,
        "new_subs_week": 6,
        "new_subs_today": 2,
        "active_users_week": 144,
        "active_users_today": 67,
        "total_tasks": 20509,
        "tasks_week": 1563,
        "tasks_today": 250,
        "total_subtasks": 48524,
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
                "username": sub.user.username,
                "start_date": sub.start_date.strftime("%Y-%m-%d"),
                "end_date": sub.end_date.strftime("%Y-%m-%d"),
                "is_active": sub.is_active,
                "transaction_id": sub.transaction_id or "-",
            }
            for sub in page.object_list
        ],
        "page": page.number,
        "total_pages": paginator.num_pages,
        "has_next": page.has_next(),
        "has_previous": page.has_previous(),
    }

    return JsonResponse(data)