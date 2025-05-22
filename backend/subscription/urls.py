from django.urls import path
import views
from subscription.views import SubscriptionView, WebhookHandler

urlpatterns = [
    path('payment/create-subscription/', SubscriptionView, name='create_subscription_payment'),
    path('yookassa/webhook/', WebhookHandler, name='yookassa_webhook'),

    # path('payment_return_page/', views.payment_return_view, name='payment_return'),
]