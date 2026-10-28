package com.opensurvey.surveys

import java.time.Instant

enum class Status { OPEN, CLOSED }
enum class SurveySort { UPDATED_DESC, REWARD_DESC, DURATION_ASC }

data class Survey(
    val source: String,
    val id: String,
    val title: String,
    val status: Status,
    val updatedAt: Instant,
    val rewardMin: Int,
    val rewardMax: Int,
    val estimatedMinutes: Int,
)

data class GetSurveysRequest(
    val query: String = "",
    val status: Status? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sort: SurveySort = SurveySort.UPDATED_DESC,
)

data class SurveyPage(val content: List<Survey>, val totalElements: Long)

interface SurveySource {
    val source: String
    fun fetchAll(): List<Survey>
}

class SourceUnavailableException(val source: String) : RuntimeException("Source $source is unavailable.")
