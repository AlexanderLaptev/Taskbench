from datetime import datetime, timezone, UTC
from django.test import SimpleTestCase
from .services import SuggestionService

class SuggestionServiceTestCase(SimpleTestCase):
    def __init__(self, method_name: str = "runTest"):
        super().__init__(method_name)
        self.SuggestionService = SuggestionService(debug=True)

    def setUp(self):
        pass

    def test_deadline_suggestion(self):
        text = 'Не забыть, что завтра в 3 часа дня созвон'
        now_time = datetime(2025, 4, 24, 12, 00, 0, tzinfo=timezone.utc)
        supposed_time = datetime(2025,4,25,15,00,0, tzinfo=timezone.utc)
        result = SuggestionService().suggest_deadline(text, now=now_time)
        self.assertEqual(result, supposed_time)
        print(result)
