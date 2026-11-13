package com.opensurvey.surveys

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SurveysApplication

fun main(args: Array<String>) {
    runApplication<SurveysApplication>(*args)
}
