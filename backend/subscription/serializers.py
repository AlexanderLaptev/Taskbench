from django.http import JsonResponse


def payment_response(payment, subscription, status):
    return JsonResponse(
        {
            'confirmation_url': payment.confirmation.confirmation_url,
            'yookassa_payment_id': payment.id,
            'subscription_id': subscription.subscription_id
        }, status=status
    )

