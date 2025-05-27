import logging
import uuid

from dateutil.utils import today
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


def is_user_subscribed(user):
    return Subscription.objects.filter(user=user, end_date__gt=today).exists()


def get_user_subscription(user):
    try:
        return Subscription.objects.get(user=user)
    except Subscription.DoesNotExist:
        raise NotFound("Subscription does not exist")


def activate_subscription(token):
    user = get_user(token)
    try:
        subscription = get_user_subscription(user)
        return recreate_subscription_payment(user, subscription)
    except NotFound as e:
        return create_subscription_payment(user)

def create_subscription_payment(user):

    subscription = Subscription.objects.create(user=user, is_active=False, start_date=timezone.now())
    payment_description = f"Оформление ежемесячной подписки для {user.email}"

    try:
        payment = create_payment(subscription=subscription, description=payment_description)
        return payment, subscription
    except Exception as e:
        subscription.delete()
        raise YooKassaError(e.args[0])

def recreate_subscription_payment(user, subscription):
    payment_description = f"Продление ежемесячной подписки для {user.email}"

    if not subscription.end_date or subscription.end_date >= timezone.now():
        payment = create_payment(subscription, payment_description)
        return payment, subscription
    else:
        subscription.activate(subscription.yookassa_payment_method_id)
        return None, subscription

def cancel_subscription(token):
    user = get_user(token)
    subscription = get_user_subscription(user)
    subscription.deactivate()


def handle_message_from_yookassa(data):
    logger.info("Got message from yookassa")
    try:
        notification_object = WebhookNotificationFactory().create(data)
        response_object = notification_object.object
        if notification_object.event == WebhookNotificationEventType.PAYMENT_SUCCEEDED:
            return handle_success(response_object)
        elif notification_object.event == WebhookNotificationEventType.PAYMENT_CANCELED:
            return handle_cancel(response_object)
        else:
            logger.error(f"Webhook notification event type {response_object.event} not supported")
    except YooKassaError as e:
        raise e
    except Exception as e:
        raise YooKassaError(e.args[0])


def handle_success(response_object):
    subscription = get_subscription_from_webhook(response_object)
    metadata = response_object.get('metadata', {})
    if metadata.get('payment_type') == "initial_subscription":
        subscription.activate(response_object.id)
        logger.info(f"Initial subscription {subscription.subscription_id} activated")
    elif metadata.get('payment_type') == "reccurring_subscription":
        subscription.renew_subscription(response_object.id)
        logger.info(f"Initial subscription {subscription.subscription_id} updated")
    else:
        raise YooKassaError(f"Payment type {response_object.event} not supported")

def handle_cancel(response_object):
    subscription = get_subscription_from_webhook(response_object)
    metadata = response_object.get('metadata', {})
    if metadata.get('payment_type') == "initial_subscription":
        subscription.delete()
        logger.info(f"Initial subscription {subscription.subscription_id} deleted, initial payment canceled")
    elif metadata.get('payment_type') == "reccurring_subscription":
        subscription.deactivate()
        logger.info(f"Initial subscription {subscription.subscription_id} deactivated, recurring payment canceled")

def create_payment(subscription, description):
    return Payment.create({
        "amount": {
            "value": SUBSCRIPTION_PRICE,
            "currency": SUBSCRIPTION_CURRENCY
        },
        "confirmation": {
            "type": "redirect",
            "return_url": f"https://{SERVER_HOST}/payment_return_page/?subscription_id={subscription.subscription_id}"
            # todo: return url
        },
        "capture": True,
        "description": description,
        "save_payment_method": True,
        "metadata": {
            "subscription_internal_id": str(subscription.subscription_id),
            "payment_type": "initial_subscription"
        }
    }, uuid.uuid4())

def create_payment_without_confirmation(subscription, description):
    return Payment.create({
        "amount": {
            "value": SUBSCRIPTION_PRICE,
            "currency": SUBSCRIPTION_CURRENCY
        },
        "capture": True,
        "description": description,
        "save_payment_method": True,
        "metadata": {
            "subscription_internal_id": str(subscription.subscription_id),
            "payment_type": "recurring_subscription"
        }
    })