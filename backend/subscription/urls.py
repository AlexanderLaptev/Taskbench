from django.urls import path

from subscription.views import SubscriptionView, WebhookHandler, UserSubscriptionStatus
from django.views.generic import TemplateView

urlpatterns = [
    path('subscription/manage/', SubscriptionView.as_view(), name='manage_subscription'),
    path('subscription/webhook/', WebhookHandler.as_view(), name='yookassa_webhook'),
    path('subscription/status/', UserSubscriptionStatus.as_view(), name='subscription_status'),
    path('payment/return/', TemplateView.as_view(template_name='payment/payment_return_page.html'), name='payment_return_page'),
]