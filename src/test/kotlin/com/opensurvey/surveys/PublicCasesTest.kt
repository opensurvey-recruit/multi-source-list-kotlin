package com.opensurvey.surveys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PublicCasesTest {
    private fun sources(): List<SurveySource> {
        val surveys = (OveySurveySource.initialExample() + PartnerSurveySource.initialExample())
        return listOf(
            OveySurveySource(surveys.filter { it.source == "OVEY" }),
            PartnerSurveySource(surveys.filter { it.source == "PARTNER" }),
        )
    }

    @Test
    fun `PUBLIC paginate all results after applying search and status filters`() {
        val repository = SurveyRepository(sources())
        val first = repository.getSurveys(GetSurveysRequest(query = "생활", status = Status.OPEN, size = 2))
        val second = repository.getSurveys(GetSurveysRequest(query = "생활", status = Status.OPEN, size = 2, page = 1))
        assertEquals(3L, first.totalElements)
        assertEquals(2, first.content.size)
        assertEquals("PARTNER/1", "${first.content.first().source}/${first.content.first().id}")
        assertEquals(3L, second.totalElements)
        assertEquals(1, second.content.size)
        assertEquals(setOf("PARTNER/1", "OVEY/1", "PARTNER/3"),
            (first.content + second.content).map { "${it.source}/${it.id}" }.toSet())
        assertEquals(first, repository.getSurveys(GetSurveysRequest(query = "생활", status = Status.OPEN, size = 2)))
    }

    @Test
    fun `PUBLIC refresh reflects additions and status changes`() {
        val initial = (OveySurveySource.initialExample() + PartnerSurveySource.initialExample())
        val ovey = OveySurveySource(initial.filter { it.source == "OVEY" })
        val partner = PartnerSurveySource(initial.filter { it.source == "PARTNER" })
        val repository = SurveyRepository(listOf(ovey, partner))
        repository.getSurveys()
        val changed = (OveySurveySource.changedExample() + PartnerSurveySource.changedExample())
        ovey.replaceSurveys(changed.filter { it.source == "OVEY" })
        partner.replaceSurveys(changed.filter { it.source == "PARTNER" })
        repository.refresh()
        val page = repository.getSurveys(GetSurveysRequest(query = "생활", status = Status.OPEN, size = 2))
        assertEquals(3L, page.totalElements)
        assertEquals(2, page.content.size)
        assertEquals("OVEY/4", "${page.content.first().source}/${page.content.first().id}")
        val all = repository.getSurveys(GetSurveysRequest(query = "생활", status = Status.OPEN, size = 100))
        assertEquals(setOf("OVEY/4", "OVEY/1", "PARTNER/3"), all.content.map { "${it.source}/${it.id}" }.toSet())
        assertEquals(all.content.take(2), page.content)
    }

    @Test
    fun `PUBLIC return an empty list and zero total when nothing matches`() {
        assertEquals(SurveyPage(emptyList(), 0), SurveyRepository(sources()).getSurveys(GetSurveysRequest(query = "없는 조사")))
    }

    @Test
    fun `PUBLIC reward sorting preserves results and duration sorting is ascending`() {
        val repository = SurveyRepository(sources())
        val request = GetSurveysRequest(query = "생활", status = Status.OPEN, size = 2)
        val rewardRequest = request.copy(sort = SurveySort.REWARD_DESC)
        val reward = repository.getSurveys(rewardRequest.copy(size = 100))
        val first = repository.getSurveys(rewardRequest)
        val second = repository.getSurveys(rewardRequest.copy(page = 1))
        val duration = repository.getSurveys(request.copy(sort = SurveySort.DURATION_ASC))
        assertEquals(3L, reward.totalElements)
        assertEquals(3, reward.content.size)
        assertEquals(setOf("OVEY/1", "PARTNER/1", "PARTNER/3"), reward.content.map { "${it.source}/${it.id}" }.toSet())
        assertEquals(3L, first.totalElements)
        assertEquals(3L, second.totalElements)
        assertEquals(reward.content, first.content + second.content)
        assertEquals(first, repository.getSurveys(rewardRequest))
        assertEquals(3L, duration.totalElements)
        assertEquals(listOf("OVEY/1", "PARTNER/3"), duration.content.map { "${it.source}/${it.id}" })
    }

    @Test
    fun `SOURCE default examples advance once after the first successful read`() {
        for (source in listOf(OveySurveySource(), PartnerSurveySource())) {
            val initial = if (source.source == "OVEY") OveySurveySource.initialExample() else PartnerSurveySource.initialExample()
            val changed = if (source.source == "OVEY") OveySurveySource.changedExample() else PartnerSurveySource.changedExample()
            val first = source.fetchAll()
            assertEquals(initial, first)
            assertEquals(changed, source.fetchAll())
            assertEquals(changed, source.fetchAll())
            assertEquals(initial, first)
        }
    }

    @Test
    fun `SOURCE failed reads do not advance examples and explicit data remains stable`() {
        val source = PartnerSurveySource(delayMillis = 0, failOnCalls = setOf(1))
        assertThrows(SourceUnavailableException::class.java) { source.fetchAll() }
        assertEquals(PartnerSurveySource.initialExample(), source.fetchAll())
        assertEquals(PartnerSurveySource.changedExample(), source.fetchAll())
        assertEquals(3, source.fetchCount)
        for (stable in sources()) {
            val first = stable.fetchAll()
            assertEquals(first, stable.fetchAll())
            assertEquals(first, stable.fetchAll())
        }
    }

}
