from django.contrib.auth import authenticate, login
from django.contrib.auth.decorators import login_required
from django.shortcuts import render, redirect
from django.http import HttpResponseForbidden
from django.views.decorators.csrf import csrf_protect, csrf_exempt


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
