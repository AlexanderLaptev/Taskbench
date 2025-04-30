from django.contrib import admin

from django.urls import path
from django.contrib import admin
from django.template.response import TemplateResponse
from django.contrib.auth.decorators import login_required

class AdminSite(admin.AdminSite):
    def get_urls(self):
        urls = super().get_urls()
        custom_urls = [
            path('custom-stats/', self.admin_view(self.custom_stats_view))
        ]
        return custom_urls + urls

    def custom_stats_view(self, request):
        context = dict(
            self.each_context(request),
            title="Custom Stats",
        )
        return TemplateResponse(request, "admin/dashboard.html", context)

admin_site = AdminSite(name='myadmin')
