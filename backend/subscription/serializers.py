from django.http import JsonResponse


def payment_response(payment, subscription, status):
    return JsonResponse(
        {
            'confirmation_url': payment.confirmation.confirmation_url,
            'yookassa_payment_id': payment.id,
            'subscription_id': subscription.subscription_id
        }, status=status
    )


def status_response(is_subscribed, subscription, user, status):
    return JsonResponse(
        {
            'is_subscribed': is_subscribed,
            'user': user.id,
            'next_payment': subscription.end_date,
            'is_active': subscription.is_active,
            'subscription_id': subscription.subscription_id
        } if subscription is not None else
        {
            'is_subscribed': is_subscribed,
            'user': user.id
        }, status=status)
