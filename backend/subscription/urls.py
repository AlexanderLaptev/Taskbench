from django.urls import path

from subscription.views import SubscriptionView, WebhookHandler

urlpatterns = [
    path('subscription/create/', SubscriptionView.as_view(), name='create_subscription_payment'),
    path('subscription/webhook/', WebhookHandler.as_view(), name='yookassa_webhook'),

]