from django.utils import timezone
from yookassa import Payment, Configuration
import uuid

from backend.settings import YOOKASSA_STORE_ID, YOOKASSA_AUTH_KEY, SUBSCRIPTION_PRICE, SUBSCRIPTION_CURRENCY
from taskbench.models.models import Subscription


@shared_task
def charge_recurring_subscriptions():
    Configuration.account_id = YOOKASSA_STORE_ID
    Configuration.secret_key = YOOKASSA_AUTH_KEY

    # Находим подписки, которые должны быть продлены сегодня
    # (is_active=True и end_date сегодня или уже прошел)
    subscriptions_to_renew = Subscription.objects.filter(
        is_active=True,
        yookassa_payment_method_id__isnull=False,
        end_date__lte=timezone.now().date()
    ).exclude(yookassa_payment_method_id__exact='')


    for sub in subscriptions_to_renew:
        if not sub.yookassa_payment_method_id:
            print(f"Subscription {sub.subscription_id} has no payment method ID. Skipping renewal.")
            sub.deactivate()
            continue

        print(f"Attempting to renew subscription {sub.subscription_id} for user {sub.user.email}")
        try:
            Payment.create({
                "amount": {
                    "value": SUBSCRIPTION_PRICE,
                    "currency": SUBSCRIPTION_CURRENCY
                },
                "capture": True,
                "payment_method_id": sub.yookassa_payment_method_id, # Используем сохраненный метод
                "description": f"Ежемесячное продление подписки для {sub.user.email} (ID: {sub.subscription_id})",
                "metadata": {
                    "subscription_internal_id": str(sub.subscription_id),
                    "payment_type": "renewal" # Пометка, что это продление
                }
            }, uuid.uuid4())
            print(f"Renewal payment initiated for subscription {sub.subscription_id}.")
        except Exception as e:
            print(f"Failed to initiate renewal payment for subscription {sub.subscription_id}: {e}")
            sub.deactivate()