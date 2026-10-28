package com.opensurvey.surveys

class SurveyRepository(private val sources: List<SurveySource>) {
    fun getSurveys(request: GetSurveysRequest = GetSurveysRequest()): SurveyPage {
        TODO("Choose a query structure and implement search, filtering, sorting, and pagination.")
    }

    fun refresh() {
        TODO("Make the latest source data available after this call.")
    }
}
