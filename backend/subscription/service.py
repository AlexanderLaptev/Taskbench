import logging
import uuid

from django.utils import timezone
from yookassa import Configuration, Payment
from yookassa.domain.notification import WebhookNotificationFactory, WebhookNotificationEventType

from backend.settings import (
    SUBSCRIPTION_PRICE,
    SUBSCRIPTION_CURRENCY,
    SERVER_HOST,
    YOOKASSA_STORE_ID,
    YOOKASSA_AUTH_KEY
)
from taskbench.models.models import Subscription
from taskbench.services.user_service import get_user
from taskbench.utils.exceptions import YooKassaError, NotFound

Configuration.account_id = YOOKASSA_STORE_ID
Configuration.secret_key = YOOKASSA_AUTH_KEY

logger = logging.getLogger(__name__)

def get_subscription_from_webhook(response_object):
    metadata = response_object.get('metadata', {})
    subscription_internal_id_str = metadata.get('subscription_internal_id')
    if not subscription_internal_id_str:
        logger.warning(
            f"YooKassa webhook has no subscription_internal_id ): "
            f"'subscription_internal_id' not found in metadata. Payout ID: {response_object.get('id')}"
        )
        raise YooKassaError("No subscription_internal_id found in metadata")
    subscription_internal_id = int(subscription_internal_id_str)
    return Subscription.objects.get(id=subscription_internal_id)

def get_subscription(subscription_id):
    try:
        return Subscription.objects.get(id=subscription_id)
    except Subscription.DoesNotExist:
        raise NotFound("Subscription does not exist")


def create_subscription_payment(token):
    user = get_user(token)

    subscription = Subscription.objects.create(user=user, is_active=False, start_date=timezone.now())
    payment_description = f"Оформление ежемесячной подписки для {user.email}"

    try:
        payment = Payment.create({
            "amount": {
                "value": SUBSCRIPTION_PRICE,
                "currency": SUBSCRIPTION_CURRENCY
            },
            "confirmation": {
                "type": "redirect",
                "return_url": f"{SERVER_HOST}/payment_return_page/?subscription_id={subscription.subscription_id}"
                # todo: return url
            },
            "capture": True,
            "description": payment_description,
            "save_payment_method": True,
            "metadata": {
                "subscription_internal_id": str(subscription.subscription_id),
                "payment_type": "initial_subscription"
            }
        }, uuid.uuid4())
        return payment, subscription
    except Exception as e:
        subscription.delete()
        raise YooKassaError(e.args[0])


def handle_message_from_yookassa(data):
    try:
        notification_object = WebhookNotificationFactory().create(data)
        response_object = notification_object.object
        if notification_object.event == WebhookNotificationEventType.PAYMENT_SUCCEEDED:
            return handle_success(response_object)
        elif notification_object.event == WebhookNotificationEventType.PAYMENT_CANCELED:
            return handle_cancel(response_object)
        else:
            raise YooKassaError(f"Webhook notification event type {response_object.event} not supported")
    except YooKassaError as e:
        raise e
    except Exception as e:
        raise YooKassaError(e.args[0])

def handle_success(response_object):
    subscription = get_subscription_from_webhook(response_object)
    subscription.renew_subscription(response_object.id)
    return 0

def handle_cancel(response_object):
    subscription = get_subscription_from_webhook(response_object)
    subscription.delete()
    return 0

