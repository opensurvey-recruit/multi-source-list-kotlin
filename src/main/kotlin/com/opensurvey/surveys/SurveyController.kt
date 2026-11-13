package com.opensurvey.surveys

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SurveyController {
    @GetMapping("/surveys")
    fun getSurveys(): Any = TODO("Combine OVEY and PARTNER surveys into one list.")
}
