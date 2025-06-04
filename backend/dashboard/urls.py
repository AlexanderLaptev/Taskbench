from django.urls import path
from dashboard.views import custom_login, dashboard_view, stats_api, subscription_list_api, subscription_page

urlpatterns = [
    path("admin/login/", custom_login, name="custom_login"),
    path("admin/dashboard/", dashboard_view, name="dashboard"),
    path("admin/subscriptions/", subscription_page, name="admin_subscriptions"),
    path("admin/dashboard/stats/", stats_api, name="stats_api"),
    path("admin/api/subscriptions/", subscription_list_api, name="subscription_list_api"),
]
