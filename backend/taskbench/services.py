from __future__ import annotations

import os
from datetime import datetime, timezone

import dateparser.search
from gigachat import GigaChat


def singleton(cls):
    _instance = None

    def wrapper(*args, **kwargs):
        nonlocal _instance
        if _instance is None:
            _instance = cls(*args, **kwargs)
        return _instance
    return wrapper

@singleton
class SuggestionService:

    def __init__(self, debug:bool=False):
        self.giga = GigaChat(
            credentials=os.getenv('GIGACHAT_API_KEY')
        )

        self.debug = debug
        if self.debug:
            return

        response = self.giga.get_token()
        self.access_token = response['access_token']
        self.expires_at = response['expires_at']

    def update_token(self):
        if self.expires_at < datetime.now(timezone.utc):
            response = self.giga.get_token()
            self.access_token = response['access_token']
            self.expires_at = response['expires_at']

    """
    Предлагает подзадачи.
    :param text: текст введенной задачи.
    """
    def suggest_subtasks(self, text: str) -> list:
        if self.debug:
            return ["subtask 1", "subtask 2", "subtask 3"]
        self.update_token()
        payload = "Предложи короткие подзадачи. Каждую подзадачу начинай с новой строки. " + text

        result = self.giga.chat(payload)
        subtasks = result.choices[0].message.split('\n')
        return subtasks

    """
    Предлагает категорию из существующих категорий у пользователя. 
    :param text: текст введенной задачи
    :param category_names: названия категорий пользователя,
    :return: индекс подходящей категории из списка.
    """
    def suggest_category(self, text: str, category_names: list) -> int | None:
        if len(category_names) == 0:
            return None

        if self.debug:
            return 0
        # names = [c.name for c in categories]
        self.update_token()
        payload = "Из категорий: " + ', '.join(category_names) + " - выбери ту что больше подходит тексту:" + text + " Напиши только номер категории."
        result_number = int(self.giga.chat(payload).choices[0].message.strip())

        return result_number

    """
    Анализирует текст с естественным языком и ищет даты.
    Выбирает либо последнюю из прошедших дат, либо ближайшую из будущих.
    :param text: анализируемый текст
    :param now: время, которое считается за текущее.
    """
    def suggest_deadline(self, text: str, *, now: datetime | None = None) -> datetime | None:
        now = now or datetime.now(tz=timezone.utc)
        now = self._make_aware(now or datetime.now())

        found = dateparser.search.search_dates(
            text,
            languages=["ru"],
            settings={
                "RELATIVE_BASE": now,
                "PREFER_DATES_FROM": "future",
                "RETURN_AS_TIMEZONE_AWARE": True,
            },
        )

        if not found:
            return None

        # found -> список кортежей (фрагмент, datetime)
        # tz = now.tzinfo
        datetimes = [self._make_aware(dt) for _, dt in found]

        future = [d for d in datetimes if d > now]
        return min(future) if future else max(datetimes)


    @staticmethod
    def _make_aware(dt: datetime, tz=timezone.utc) -> datetime:
        """Возвращает datetime с tzinfo (добавляет tz, если его нет)."""
        return dt if dt.tzinfo else dt.replace(tzinfo=tz)