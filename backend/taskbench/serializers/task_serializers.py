from rest_framework import serializers

class TaskDPCtoFlatSerializer(serializers.Serializer):
    category_id = serializers.IntegerField(required=False, allow_null=True)
    priority = serializers.IntegerField(required=False, allow_null=True)
    deadline = serializers.DateTimeField(required=False, allow_null=True)
    title = serializers.CharField()
    timestamp = serializers.DateTimeField()

    def to_internal_value(self, data):
        # Flatten 'dpc' into the main data
        dpc_data = data.get('dpc', {})
        if dpc_data:
            # Merge dpc fields into main data
            data = {**dpc_data, **data}
            data.pop('dpc', None)  # Remove 'dpc' after merging

        return super().to_internal_value(data)
