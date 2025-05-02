from __future__ import annotations

import logging
import os
import re
from logging import Logger, INFO

import dateparser.search
from datetime import datetime, timezone
from gigachat import GigaChat
from typing import Union

GIGACHAT_API_SAFETY_GAP = 60

logger = logging.getLogger(__name__)

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
            credentials=os.getenv('GIGACHAT_AUTH_KEY'),
            verify_ssl_certs=False
        )

        self.debug = debug
        if self.debug:
            return

        response = self.giga.get_token()

        if response is None:
            raise RuntimeError("Не удалось получить токен от GigaChat")

        self.access_token = response.access_token
        self.expires_at = datetime.fromtimestamp(response.expires_at / 1000, tz=timezone.utc)

    def update_token(self):
        if self.expires_at < datetime.now(timezone.utc):
            response = self.giga.get_token()

            if response is None:
                raise RuntimeError("Не удалось получить токен от GigaChat")

            logger.info('updating GIGACHAT token')

            self.access_token = response.access_token
            self.expires_at = datetime.fromtimestamp(response.expires_at / 1000, tz=timezone.utc)

    def suggest_subtasks(self, text: str) -> list:
        """
        Предлагает подзадачи.
        :param text: текст введенной задачи.
        """
        if self.debug:
            return ["1. Начать делать задачу", "2. Продолжить делать задачу", "3. Закончить делать задачу"]
        self.update_token()
        payload = 'Разбей данную задачу на список максимально коротких подзадач. Каждый элемент начинай с новой строки без иных символов и нумерации. ' + text

        result = self.giga.chat(payload)
        subtasks = [
            match.group(1).strip().lower()
            for line in result.choices[0].message.content.split('\n')
            if (match := re.match(r'^(?:\d+\.\s*|-\s*)?([^.]+)(?:\.?)$', line.strip()))]

        return subtasks

    def suggest_category(self, text: str, category_names: list) -> int | None:
        """
        Предлагает категорию из существующих категорий у пользователя.
        :param text: текст введенной задачи
        :param category_names: названия категорий пользователя,
        :return: индекс подходящей категории из списка, -1 если не нашло подходящее.
        """

        if len(category_names) == 0:
            return -1

        if self.debug:
            return 0
        # names = [c.name for c in categories]
        self.update_token()
        payload = "Выбери из списка категорию, которая больше всего подходит тексту. Напиши только выбранное. Список:" +', '.join(category_names) + " Текст:" + text
        result = self.giga.chat(payload).choices[0].message.content
        print("Название выбранной категории", result)

        for i in range(len(category_names)):
            if self._equal_ignore_space_case(category_names[i], result):
                return i

        return -1

    def suggest_priority(self, text: str) -> int:
        """
        Предлагает приоритет из 0/1.
        :param text: текст введенной задачи
        :return: 0, если не очень важно, 1 если очень важно
        """
        if self.debug:
            return 0
        self.update_token()
        payload = "Предположи важность задачи. 0, если не очень важно, 1 если очень важно. Напиши только число." + text
        try:
            result_number = int(self.giga.chat(payload).choices[0].message.content)
            return result_number
        except (ValueError, TypeError):
            return 0

    def suggest_deadline(self, text: str, *, now: datetime | None = None) -> str | None:
        """
        Анализирует текст с естественным языком и ищет даты.
        Выбирает либо последнюю из прошедших дат, либо ближайшую из будущих.
        :param text: анализируемый текст
        :param now: время, которое считается за текущее.
        """
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
        return (min(future) if future else max(datetimes)).astimezone(timezone.utc).isoformat().replace('+00:00', 'Z')


    @staticmethod
    def _make_aware(dt: datetime, tz=timezone.utc) -> datetime:
        """Возвращает datetime с tzinfo (добавляет tz, если его нет)."""
        return dt if dt.tzinfo else dt.replace(tzinfo=tz)

    @staticmethod
    def _equal_ignore_space_case(a: Union[str, bytes], b: Union[str, bytes]) -> bool:
        """
        Возвращает True, если строки 'a' и 'b' совпадают,
        игнорируя все виды пробелов (space, tab, NBSP, \n …) и регистр символов.
        """
        if isinstance(a, bytes):
            a = a.decode()
        if isinstance(b, bytes):
            b = b.decode()

        # убираем всё, что считается пробельным в Unicode (\s = [ \t\n\r\f\v] + другие)
        normalize = lambda s: re.sub(r'\s+', '', s).casefold()

        return normalize(a) == normalize(b)