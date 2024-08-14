package com.example.calenderapp
import java.time.DayOfWeek
import java.time.LocalTime

data class Event(
    val id: Int,
    var title: String,
    var daysOfWeek: List<DayOfWeek>,
    var startTime: LocalTime,
    var endTime: LocalTime,
    var location: String,
    var description: String = "" // Optional description
)
