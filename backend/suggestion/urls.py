from django.urls import path

from suggestion.views import SuggestionView

urlpatterns = [
    path('ai/suggestions/', SuggestionView.as_view(), name="ai_suggestions"),
]
