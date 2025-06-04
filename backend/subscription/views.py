import json
import logging

from rest_framework.exceptions import ValidationError
from rest_framework.response import Response
from rest_framework.views import APIView

from subscription.serializers import payment_response, status_response
from subscription.service import handle_message_from_yookassa, is_user_subscribed, \
    cancel_subscription, activate_subscription, get_user_subscription
from taskbench.services.user_service import get_token, get_user
from taskbench.utils.exceptions import YooKassaError, AuthenticationError, NotFound

logger = logging.getLogger(__name__)

class SubscriptionView(APIView):
    def post(self, request, *args, **kwargs):
        try:
            token = get_token(request)
            payment, subscription = activate_subscription(token=token)
            return payment_response(payment=payment, subscription=subscription, status=201)
        except AuthenticationError as e:
            return Response({'error': e.message}, status=401)
        except YooKassaError as e:
            return Response({'error': e.message}, status=500)

    def delete(self, request, *args, **kwargs):
        try:
            token = get_token(request)
            cancel_subscription(token)
            return Response(status=204)
        except NotFound as e:
            return Response({'error': e.message}, status=400)
        except AuthenticationError as e:
            return Response({'error': e.message}, status=401)
        except Exception as e:
            return Response({'error': e.args[0]}, status=500)


class WebhookHandler(APIView):
    def post(self, request, *args, **kwargs):
        event_json = json.loads(request.body)

        # yookassa_ips = ['185.71.76.0/27', '185.71.77.0/27', '77.75.153.0/25', '77.75.156.35', '77.75.156.11', '77.75.154.128/25']
        # client_ip = request.META.get('REMOTE_ADDR')
        # if not any(ip_network(client_ip).subnet_of(ip_network(net)) for net in yookassa_ips):
        #     print(f"Webhook from untrusted IP: {client_ip}")
        #     return Response(status=403)

        try:
            handle_message_from_yookassa(data=event_json)
            return Response(status=200)
        except ValidationError as e:
            logger.error(e.args[0])
            return Response(status=200)
        except NotFound as e:
            logger.error(e.message)
            return Response(status=200)
        except json.decoder.JSONDecodeError as e:
            logger.error(e.args[0])
            return Response(status=200)
        except Exception as e:
            logger.error(e.args[0])
            return Response(status=200)


class UserSubscriptionStatus(APIView):
    def get(self, request, *args, **kwargs):
        token = get_token(request)

        try:
            user = get_user(token)
            try:
                subscription = get_user_subscription(user)
            except NotFound as e:
                subscription = None
            return status_response(user=user, is_subscribed=is_user_subscribed(user), subscription=subscription, status=200)
        except NotFound as e:
            return Response(status=404)
        except AuthenticationError as e:
            return Response(status=401)
