from __future__ import annotations

import logging
import os
import re
from datetime import datetime, timezone
from typing import Union

import dateparser.search
from gigachat import GigaChat
from gigachat.models import Chat, Messages, MessagesRole
from pydantic import ValidationError

from subscription.service import is_user_subscribed
from taskbench.models.models import Category
from taskbench.serializers.task_serializers import TaskDPCtoFlatSerializer
from taskbench.services.user_service import get_user
from taskbench.utils.decorators import singleton

GIGACHAT_API_SAFETY_GAP = 60

logger = logging.getLogger(__name__)

SUBTASK_SYSTEM_PROMPT = """
Разбей введенную пользователем задачу на несколько более мелких подзадач, состоящие не более чем из четырех слов.
Каждая подзадача должна быть короткой и представлена на отдельной строке.
Не используй знаки препинания или обозначения списка, просто пиши только подзадачи с новой строки.
"""

SUBTASK_SYSTEM_PROMPT_V2 = """
Предложи несколько мелких подзадач к введенной пользователем задаче, состоящих не более чем из четырех слов.
Каждая подзадача должна быть короткой и представлена на отдельной строке.
Не пиши заголовок.
Не пиши нумерацию подзадачи, пиши ТОЛЬКО текст подзадач с новой строки.
"""

TIME_SYSTEM_PROMPT = """
Предложи предположительную дату и время, соответствующие сроку введенной пользователем задачи.
Если время указано относительно, например словами 'завтра' или 'в следующую среду', в качестве точки отсчета используй текущее время.
В приоритете всегда предполагай время из будущего.
Если и время и дата не указаны, отправь ТОЛЬКО ОДИН СИМВОЛ: "-".
Если известно только время, считай датой сегодня.
Если известна только дата, считай время таким же как сейчас.
Отправь ТОЛЬКО дату и время в формате YYYY:MM:DD hh:mm. Не добавляй никакого другого текста или пояснений.
Например: 2024:03:15 10:30
"""

CATEGORY_SYSTEM_PROMPT = """
Соотнеси пользовательский текст с одной из категорий, соответствующих следующему списку. Напиши только одно слово - название категории. Список категорий:\n
"""


def get_subtask_prompt():
    return SUBTASK_SYSTEM_PROMPT_V2


def get_time_system_prompt(user_datetime):
    return TIME_SYSTEM_PROMPT + "\nТекущее время (точка отсчета): " + user_datetime.isoformat(timespec='minutes')


def get_category_system_prompt(category_names: list):
    return CATEGORY_SYSTEM_PROMPT + ', '.join(category_names)


def suggest(token, data):
    """

    :param token:
    :param data:
    :return: subtasks, category names (list), category_id, deadline
    """

    user = get_user(token)
    serializer = TaskDPCtoFlatSerializer(data=data)
    if not serializer.is_valid():
        return ValidationError(serializer.errors)
    input_data = serializer.validated_data
    deadline = input_data.get('deadline')
    title = input_data.get('title')
    category_id = input_data.get('category_id')
    timestamp = input_data.get('timestamp')

    service = SuggestionService(debug=False)

    subscribed = is_user_subscribed(user)
    # subscribed = True

    if deadline is None:
        if not subscribed:
            deadline = service.suggest_deadline_local(title, now=timestamp)
        else:
            deadline = service.suggest_deadline(title, now=timestamp)

    """
        Проверка пользователя на подписку.
    """
    if not subscribed:
        return None, None, None, deadline

    if category_id is None:
        categories = Category.objects.filter(user=user)
        category_names = [c.name for c in categories]
        category_index = service.suggest_category(title, category_names)
        category_name = ''
        if category_index < 0 or category_index >= len(categories):
            category_id = None
        else:
            category_id = categories[category_index].category_id
            category_name = categories[category_index].name
    else:
        category_name = Category.objects.get(category_id=category_id).name

    subtasks = service.suggest_subtasks(title)

    return subtasks, category_name, category_id, deadline


@singleton
class SuggestionService:

    def __init__(self, debug: bool = False):
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

    def send_message_with_system_prompt(self, system_prompt: str, user_text: str):
        if self.debug: return None

        self.update_token()
        result = self.giga.chat(
            Chat(
                messages=[
                    Messages(
                        role=MessagesRole.SYSTEM,
                        content=system_prompt
                    ),
                    Messages(
                        role=MessagesRole.USER,
                        content=user_text
                    )
                ]
            )
        )
        return result

    def suggest_subtasks(self, text: str) -> list:
        """
        Предлагает подзадачи.
        :param text: текст введенной задачи.
        """
        if self.debug:
            return ["1. Начать делать задачу", "2. Продолжить делать задачу", "3. Закончить делать задачу"]
        self.update_token()

        result = self.send_message_with_system_prompt(get_subtask_prompt(), text)
        subtasks = [
            match.group(1).strip().lower()
            for line in result.choices[0].message.content.split('\n')
            if (match := re.match(r'^(?:\d+\.\s*|-\s*)?([^.]+)\.?$', line.strip()))]

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
        self.update_token()
        result = self.send_message_with_system_prompt(
            get_category_system_prompt(category_names),
            text).choices[0].message.content

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

    def suggest_deadline(self, text: str, *, now: datetime | None = None) -> datetime | None:
        """
        Анализирует текст с использованием gigachat и ищет даты.
        :param text: анализируемый текст
        :param now: время, которое считается за текущее.
        """

        if self.debug:
            return self.suggest_deadline_local(text, now=now)

        now = now or datetime.now().replace(tzinfo=None)

        result = self.send_message_with_system_prompt(
            get_time_system_prompt(now),
            text).choices[0].message.content

        cleaned_text = result.strip()
        if cleaned_text == "-":
            return self.suggest_deadline_local(text, now=now)

        expected_format = "%Y:%m:%d %H:%M"

        try:
            dt_object = datetime.strptime(cleaned_text, expected_format)
            return dt_object
        except Exception as e:
            local_suggest = self.suggest_deadline_local(cleaned_text, now=now)
            if local_suggest is not None:
                return local_suggest
            local_suggest = self.suggest_deadline_local(text, now=now)
            return local_suggest

    def suggest_deadline_local(self, text: str, *, now: datetime | None = None) -> datetime | None:
        """
        Анализирует текст локально с естественным языком и ищет даты.
        Выбирает либо последнюю из прошедших дат, либо ближайшую из будущих.
        :param text: анализируемый текст
        :param now: время, которое считается за текущее.
        """
        now = now or datetime.now().replace(tzinfo=None)

        found = dateparser.search.search_dates(
            text,
            languages=["ru"],
            settings={
                "RELATIVE_BASE": now,
                "PREFER_DATES_FROM": "future",
                "RETURN_AS_TIMEZONE_AWARE": False,
            },
        )

        if not found:
            return None

        datetimes = [dt for _, dt in found]

        future = [d.replace(tzinfo=None) for d in datetimes if d.replace(tzinfo=None) > now.replace(tzinfo=None)]
        return min(future) if future else max(datetimes)

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

        normalize = lambda s: re.sub(r'\s+', '', s).casefold()

        return normalize(a) == normalize(b)
