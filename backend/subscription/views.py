import json

from rest_framework.exceptions import ValidationError
from rest_framework.response import Response
from rest_framework.views import APIView

from subscription.serializers import payment_response
from subscription.service import create_subscription_payment, handle_payment
from taskbench.utils.exceptions import YooKassaError, AuthenticationError, NotFound


class SubscriptionView(APIView):
    def post(self, request, *args, **kwargs):
        try:
            token = request.data.get('token')
            payment, subscription = create_subscription_payment(token=token)
            payment_response(payment=payment, subscription=subscription, status=201)
        except AuthenticationError as e:
            return Response({'error': e.message}, status=401)
        except YooKassaError as e:
            return Response({'error': e.message}, status=500)




class WebhookHandler(APIView):
    def post(self, request, *args, **kwargs):
        data = json.loads(request.body)

        # todo: проверить ip адрес запроса
        # yookassa_ips = ['185.71.76.0/27', '185.71.77.0/27', '77.75.153.0/25', '77.75.154.128/25', '2a02:5180::/32']
        # client_ip = request.META.get('REMOTE_ADDR')
        # if not any(ip_network(client_ip).subnet_of(ip_network(net)) for net in yookassa_ips):
        #     print(f"Webhook from untrusted IP: {client_ip}")
        #     return Response(status=403)
        try:
            handle_payment(data=data)
            Response(status=200)
        except ValidationError as e:
            return Response(status=200)
        except NotFound as e:
            return Response(status=200)
        except json.decoder.JSONDecodeError as e:
            return Response(status=400)
        except Exception as e:
            return Response(status=200)
