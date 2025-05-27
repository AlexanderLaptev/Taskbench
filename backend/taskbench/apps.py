import os

from django.apps import AppConfig

class TaskbenchConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'taskbench'

    def ready(self):
        if os.environ.get('RUN_MAIN', None) != 'true':
            return
        from taskbench import scheduler
        scheduler.start_scheduler_for_subscriptions()