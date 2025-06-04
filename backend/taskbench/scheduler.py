import logging

from apscheduler.schedulers.background import BackgroundScheduler
from django_apscheduler.jobstores import DjangoJobStore

from backend import settings
from subscription.tasks import charge_recurring_subscriptions

logger = logging.getLogger(__name__)

def start_scheduler_for_subscriptions():
    scheduler = BackgroundScheduler(timezone=settings.TIME_ZONE) # Используем TIME_ZONE из settings.py
    scheduler.add_jobstore(DjangoJobStore(), "default")

    job_id = "daily_subscription_charger"

    existing_job = scheduler.get_job(job_id, jobstore="default")
    if existing_job:
        logger.info(f"Deleting existing task {job_id}.")
        scheduler.remove_job(job_id, jobstore="default")

    scheduler.add_job(
        charge_recurring_subscriptions,
        trigger='cron',
        hour='3',
        minute='00',
        id=job_id,
        replace_existing=True,
        jobstore="default"
    )
    logger.info(f"Task '{job_id}' added and will be started at 03:00.")

    try:
        scheduler.start()
        logger.info("APScheduler started.")
    except Exception as e:
        logger.error(f"APScheduler start failed: {e}")