import os
import uuid

from django.utils import timezone
from rest_framework.exceptions import ValidationError
from yookassa import Configuration, Payment

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
            "capture": True,  # Одностадийная оплата
            "description": payment_description,
            "save_payment_method": True,  # !ВАЖНО! Сохраняем способ оплаты для автопродления
            "metadata": {
                "subscription_internal_id": str(subscription.subscription_id),
                "payment_type": "initial_subscription"  # Пометка типа платежа
            }
        }, uuid.uuid4())
        return payment, subscription
    except Exception as e:
        subscription.delete()
        raise YooKassaError(e)


def handle_payment(data):
    payment_object = data.get('object')

    if not payment_object:
        raise ValidationError("Payment object is missing")

    yookassa_payment_id = payment_object.get('id')
    status = payment_object.get('status')
    metadata = payment_object.get('metadata', {})
    subscription_internal_id = metadata.get('subscription_internal_id')
    payment_type = metadata.get('payment_type', 'unknown')
    event = data.get('event')

    if not subscription_internal_id:
        print(f"Webhook: 'subscription_internal_id' not found in metadata for payment {yookassa_payment_id}")
        raise NotFound("No subscription internal id")  # Отвечаем ОК, чтобы ЮKassa не повторяла

    subscription = get_subscription(subscription_internal_id)

    if event == 'payment.succeeded':
        if payment_type == 'initial_subscription':
            payment_method_details = payment_object.get('payment_method')
            yookassa_payment_method_id = None
            if payment_method_details and payment_method_details.get('saved') and payment_method_details.get('id'):
                yookassa_payment_method_id = payment_method_details.get('id')

            subscription.activate(
                yk_payment_id=yookassa_payment_id,
                yk_payment_method_id_from_payment=yookassa_payment_method_id
            )
        elif payment_type == 'renewal':
            subscription.renew_subscription(yk_renewal_payment_id=yookassa_payment_id)
        else:
            print(f"Webhook: Unknown payment_type '{payment_type}' for successful payment {yookassa_payment_id}")
    elif event == 'payment.canceled':
        subscription.deactivate()
    elif event == 'payment.waiting_for_capture':
        print(f"Payment {yookassa_payment_id} is waiting_for_capture. (Should not happen with capture=True)")
    return
