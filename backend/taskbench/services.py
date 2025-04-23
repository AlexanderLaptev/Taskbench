from __future__ import annotations

import os
from gigachat import GigaChat
from datetime import datetime, timezone
from natasha import DatesExtractor, MorphVocab
import dateparser

# Используется, чтобы не тратить токены в Gigachat. True - шаблоны, False - обращение к нейросети.
DEBUG = True

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

    def __init__(self):
        self.giga = GigaChat(credentials=os.getenv('GIGACHAT_API_KEY'))
        response = self.giga.get_token()
        # pass

    """
    Предлагает подзадачи.
    :param text: текст введенной задачи.
    """
    def suggest_subtasks(self, text: str) -> list:
        if DEBUG:
            return ["subtask 1", "subtask 2", "subtask 3"]

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

        if DEBUG:
            return 0

        # names = [c.name for c in categories]
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

        morph = MorphVocab()
        extractor = DatesExtractor(morph)
        spans = extractor(text)

        parsed = []
        for span in spans:
            dt = self._parse_date(span.text, now)
            if dt:
                parsed.append(dt)

        if not parsed:
            return None

        future = [d for d in parsed if d > now]
        if future:
            return min(future)  # ближайшая будущая
        return max(parsed)  # иначе последняя (из прошедших)

    """
    Парсит дату/время из естественного текста в datetime формат
    :return: полученный datetime или None, если не получилось 
    """
    def _parse_date(self, text: str, now: datetime)  -> datetime | None:
        return dateparser.parse(
            text,
            languages=['ru'],
            settings={
                "RELATIVE_BASE": now,
                "PREFER_DATES_FROM": "future",
                "TIMEZONE": "Europe/Moscow",
                "RETURN_AS_TIMEZONE_AWARE": True
            },
        )