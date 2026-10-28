package com.opensurvey.surveys

import java.time.Instant
import kotlin.time.TimeSource

// Only full reads are available, so detecting changes requires a fetchAll call.
class OveySurveySource(
    initialSurveys: List<Survey>,
) : SurveySource {
    override val source: String = "OVEY"
    private var surveys = initialSurveys.toList()
    private var nextSurveys: List<Survey>? = null
    var fetchCount: Int = 0
        private set

    constructor() : this(initialExample()) {
        nextSurveys = changedExample()
    }

    override fun fetchAll(): List<Survey> {
        val started = TimeSource.Monotonic.markNow()
        var outcome = "failed"
        try {
            fetchCount += 1
            val result = surveys.toList()
            // Advance after a successful read even if the repository never reads again.
            nextSurveys?.let { surveys = it }
            nextSurveys = null
            outcome = "success"
            return result
        } finally {
            println("source=$source fetchCount=$fetchCount outcome=$outcome elapsed=${started.elapsedNow().inWholeMilliseconds}ms")
        }
    }

    fun replaceSurveys(surveys: List<Survey>) {
        require(surveys.all { it.source == source })
        require(surveys.map { it.id }.distinct().size == surveys.size)
        this.surveys = surveys.toList()
        nextSurveys = null
    }

    init {
        replaceSurveys(initialSurveys)
    }

    companion object {
        fun initialExample(): List<Survey> = listOf(
            Survey("OVEY", "1", "식생활 조사", Status.OPEN, Instant.parse("2026-10-13T09:00:00Z"), 300, 300, 7),
            Survey("OVEY", "2", "라이프 스타일 조사", Status.OPEN, Instant.parse("2026-10-13T09:30:00Z"), 200, 200, 5),
            Survey("OVEY", "3", "생활용품 조사", Status.CLOSED, Instant.parse("2026-10-13T10:00:00Z"), 500, 500, 12),
        )

        fun changedExample(): List<Survey> = listOf(
            Survey("OVEY", "1", "식생활 조사", Status.OPEN, Instant.parse("2026-10-13T09:00:00Z"), 300, 300, 7),
            Survey("OVEY", "2", "라이프 스타일 조사", Status.OPEN, Instant.parse("2026-10-13T09:30:00Z"), 200, 200, 5),
            Survey("OVEY", "3", "생활용품 조사", Status.CLOSED, Instant.parse("2026-10-13T10:00:00Z"), 500, 500, 12),
            Survey("OVEY", "4", "여가생활 조사", Status.OPEN, Instant.parse("2026-10-13T11:00:00Z"), 700, 700, 3),
        )
    }
}
