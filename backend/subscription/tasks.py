import logging

from apscheduler.schedulers.background import BackgroundScheduler
from django.utils import timezone
from yookassa import Configuration

from backend.settings import YOOKASSA_STORE_ID, YOOKASSA_AUTH_KEY
from subscription.service import create_payment_without_confirmation
from taskbench.models.models import Subscription

logger = logging.getLogger(__name__)

def charge_recurring_subscriptions():
    Configuration.account_id = YOOKASSA_STORE_ID
    Configuration.secret_key = YOOKASSA_AUTH_KEY

    subscriptions_to_renew = Subscription.objects.filter(
        is_active=True,
        yookassa_payment_method_id__isnull=False,
        end_date__lte=timezone.now().date()
    ).exclude(yookassa_payment_method_id__exact='')


    for sub in subscriptions_to_renew:
        if not sub.yookassa_payment_method_id:
            logger.info(f"Subscription {sub.subscription_id} has no payment method ID. Skipping renewal.")
            sub.deactivate()
            continue

        logger.info(f"Attempting to renew subscription {sub.subscription_id} for user {sub.user.email}")
        try:
            create_payment_without_confirmation(sub, f"Продление ежемесячной подписки для {sub.user.email}")
            logger.info(f"Renewal payment initiated for subscription {sub.subscription_id}.")
        except Exception as e:
            logger.info(f"Failed to initiate renewal payment for subscription {sub.subscription_id}: {e}")
            sub.deactivate()