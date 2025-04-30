from django.urls import path
from dashboard.views import custom_login, dashboard_view

urlpatterns = [
    path("admin/login/", custom_login, name="custom_login"),
    path("admin/dashboard/", dashboard_view, name="dashboard"),
]
