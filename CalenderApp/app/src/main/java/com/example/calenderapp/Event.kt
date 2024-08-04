package com.example.calenderapp
import java.time.DayOfWeek
import java.time.LocalTime
import kotlin.random.Random


data class Event(
    val id: Int, // could be used by the size of the repository
    var title: String,
    var daysOfWeek: List<DayOfWeek>,
    var startTime: LocalTime,
    var endTime: LocalTime,
    var location: String,
    var description: String = "" // Optional description
)
