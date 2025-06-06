# Generated by Django 5.2 on 2025-05-25 10:42

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('taskbench', '0005_user_access_at'),
    ]

    operations = [
        migrations.RenameField(
            model_name='subscription',
            old_name='transaction_id',
            new_name='latest_yookassa_payment_id',
        ),
        migrations.AddField(
            model_name='subscription',
            name='yookassa_payment_method_id',
            field=models.CharField(blank=True, max_length=255, null=True),
        ),
        migrations.AlterField(
            model_name='subscription',
            name='end_date',
            field=models.DateTimeField(blank=True, null=True),
        ),
        migrations.AlterField(
            model_name='subscription',
            name='is_active',
            field=models.BooleanField(default=False),
        ),
    ]
