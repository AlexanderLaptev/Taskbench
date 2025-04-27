from rest_framework import serializers


class StatisticsSerializer(serializers.Serializer):
    done_today = serializers.IntegerField()
    max_done = serializers.IntegerField()
    weekly = serializers.ListField(
        child=serializers.FloatField(),
        min_length=7,
        max_length=7
    )