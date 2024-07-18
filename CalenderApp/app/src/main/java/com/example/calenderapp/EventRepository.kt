package com.example.calenderapp

import kotlin.random.Random

object EventRepository {

    var counter: Int = 0
        private set

    var events: ArrayList<Event> = ArrayList()
        private set

    var idsSet: MutableSet<Int> = mutableSetOf()
        private set


    fun generateId(): Int {
        var randomNumber = Random.nextInt(1, 100)

        while (randomNumber in idsSet){
            randomNumber = Random.nextInt(1, 100)
        }
        idsSet.add(randomNumber)
        return randomNumber
    }


    fun incrementCounter(){
        counter++
    }

    fun getEventsForWeek(): ArrayList<Event> {
        // In a real app, you would filter by the current week's dates
        return events
    }

    fun addEvent(event: Event): Boolean {
        if (doesOverlap(event)){
            // cannot add the event
            return false
        }
        events.add(event)
        return true // added event successfully
    }

//    overlapping needs to be checked
    fun updateEvent(event: Event) {
        val index = events.indexOfFirst { it.id == event.id }
        if (index != -1) {
            events[index] = event
        }
    }

    fun deleteEvent(event: Event) {
        events.remove(event)
    }


    private fun doesOverlap(event: Event): Boolean {
        for (e in events){
            if (event.daysOfWeek.intersect(e.daysOfWeek).isNotEmpty() &&
                (event.startTime <= e.endTime && event.endTime >= e.startTime)){
                // there is an overlap
                return true
            }
        }
        return false
    }

}