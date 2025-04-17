#!/bin/sh

echo "Applying database migrations..."
#python manage.py makemigrations
python manage.py migrate

echo "Marking container as ready..."
touch /tmp/app_ready

echo "Starting Gunicorn..."
exec gunicorn --bind 0.0.0.0:8000 --workers 3 backend.wsgi:application
