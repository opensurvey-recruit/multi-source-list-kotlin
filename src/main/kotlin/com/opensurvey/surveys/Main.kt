package com.opensurvey.surveys

import kotlin.time.TimeSource

fun main(args: Array<String>) {
    require(args.size <= 1) { "Specify one scenario: fast, slow, or failure." }
    try {
        val repository = SurveyRepository(listOf(OveySurveySource(), PartnerSurveySource(args.firstOrNull() ?: "fast")))
        val request = GetSurveysRequest(query = "생활", status = Status.OPEN, size = 2)

        // 각 원본은 첫 fetchAll() 성공 시 초기 데이터를 반환한 뒤 내부 데이터를 바꾼다.
        observe("initial query") { repository.getSurveys(request) }

        // 반복 조회로 변경 반영 시점과 원본 호출 횟수를 비교한다.
        repeat(3) { index ->
            observe("query after source changes ${index + 1}") { repository.getSurveys(request) }
        }

        // 갱신 성공 또는 실패 뒤 조회 결과를 확인한다.
        observe("refresh") { repository.refresh(); null }
        observe("query after refresh") { repository.getSurveys(request) }

        // 검색어와 상태 조건을 유지하고 정렬에 따른 순서 차이를 비교한다.
        observe("reward descending") { repository.getSurveys(request.copy(sort = SurveySort.REWARD_DESC)) }
        observe("duration ascending") { repository.getSurveys(request.copy(sort = SurveySort.DURATION_ASC)) }
    } catch (error: NotImplementedError) {
        println("Starter code is running. Implement both TODOs in SurveyRepository.kt.")
    }
}

private fun observe(label: String, action: () -> SurveyPage?) {
    val started = TimeSource.Monotonic.markNow()
    try {
        println("$label: ${action() ?: "completed"}")
    } catch (error: SourceUnavailableException) {
        println("$label failed: ${error.message}")
    } finally {
        println("query elapsed=${started.elapsedNow().inWholeMilliseconds}ms")
    }
}
