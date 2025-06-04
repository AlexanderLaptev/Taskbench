from rest_framework import serializers
from django.http import JsonResponse

class StatisticsSerializer(serializers.Serializer):
    done_today = serializers.IntegerField()
    max_done = serializers.IntegerField()
    weekly = serializers.ListField(
        child=serializers.FloatField(),
        min_length=7,
        max_length=7
    )

def statistics_response(statistics, status=200):
    serializer = StatisticsSerializer(data=statistics)  # Передаем данные через data
    if not serializer.is_valid():
        raise serializers.ValidationError(serializer.errors)
    return JsonResponse(serializer.data, status=status)