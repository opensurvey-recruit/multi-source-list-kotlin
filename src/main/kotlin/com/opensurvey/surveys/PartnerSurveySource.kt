package com.opensurvey.surveys

import java.time.Instant
import kotlin.time.TimeSource

// Fixed failure call numbers make each scenario reproducible.
class PartnerSurveySource(
    initialSurveys: List<Survey>,
    private val delayMillis: Long = 0,
    failOnCalls: Set<Int> = emptySet(),
) : SurveySource {
    override val source: String = "PARTNER"
    private var surveys = initialSurveys.toList()
    private var nextSurveys: List<Survey>? = null
    private val failOnCalls = failOnCalls.toSet()
    var fetchCount: Int = 0
        private set

    init {
        require(delayMillis in 0..2_000)
        require(failOnCalls.all { it > 0 })
        replaceSurveys(initialSurveys)
    }

    constructor(delayMillis: Long, failOnCalls: Set<Int> = emptySet()) :
        this(initialExample(), delayMillis, failOnCalls) {
        nextSurveys = changedExample()
    }

    constructor(scenario: String = "fast") : this(
        delayMillis = if (scenario == "slow") 500 else 0,
        failOnCalls = if (scenario == "failure") setOf(2) else emptySet(),
    ) {
        require(scenario in setOf("fast", "slow", "failure")) { "Unknown scenario: $scenario" }
    }

    override fun fetchAll(): List<Survey> {
        val started = TimeSource.Monotonic.markNow()
        var outcome = "failed"
        try {
            fetchCount += 1
            if (delayMillis > 0) Thread.sleep(delayMillis)
            if (fetchCount in failOnCalls) throw SourceUnavailableException(source)
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

    companion object {
        fun initialExample(): List<Survey> = listOf(
            Survey("PARTNER", "1", "일상생활 관련 조사(해외조사)", Status.OPEN, Instant.parse("2026-10-13T09:45:00Z"), 100, 1000, 15),
            Survey("PARTNER", "2", "Shopping habits survey", Status.OPEN, Instant.parse("2026-10-13T08:00:00Z"), 150, 150, 4),
            Survey("PARTNER", "3", "주거생활 조사", Status.OPEN, Instant.parse("2026-10-13T09:00:00Z"), 300, 1000, 10),
        )

        fun changedExample(): List<Survey> = listOf(
            Survey("PARTNER", "1", "일상생활 관련 조사(해외조사)", Status.CLOSED, Instant.parse("2026-10-13T10:30:00Z"), 100, 1000, 15),
            Survey("PARTNER", "2", "Shopping habits survey", Status.OPEN, Instant.parse("2026-10-13T08:00:00Z"), 150, 150, 4),
            Survey("PARTNER", "3", "주거생활 조사", Status.OPEN, Instant.parse("2026-10-13T09:00:00Z"), 300, 1000, 10),
        )
    }
}
