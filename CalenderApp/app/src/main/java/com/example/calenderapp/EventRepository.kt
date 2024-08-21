package com.example.calenderapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.Calendar

object EventRepository {

    private val db = FirebaseFirestore.getInstance()

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

    fun getEvenById(eventId: Int): Event? {
        val event = events.find { it.id == eventId }

        return event
    }
    fun getEventsFromFirebase(context: Context, callback: (List<Event>) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w("Firestore", "User not logged in")
            callback(emptyList())
            return
        }

        db.collection("users").document(userId).collection("events")
            .get()
            .addOnSuccessListener { documents ->
                val events = documents.mapNotNull { document ->
                    try {
                        Log.w("Firestore", "in the try block")

                        val id = document.getLong("id")?.toInt() ?: return@mapNotNull null
                        val title = document.getString("title") ?: return@mapNotNull null
                        val daysOfWeekStrings = document.get("daysOfWeek") as? List<String> ?: return@mapNotNull null
                        val location = document.getString("location") ?: return@mapNotNull null
                        val description = document.getString("description") ?: ""

                        val daysOfWeek = daysOfWeekStrings.map { DayOfWeek.valueOf(it) }
                        val startTime = parseTimeObject(document.get("startTime") as? Map<String, Any>) ?: return@mapNotNull null
                        val endTime = parseTimeObject(document.get("endTime") as? Map<String, Any>) ?: return@mapNotNull null
                        Log.w("Firestore", "in the try block processed all the entries")

                        val event = Event(
                            id = id,
                            title = title,
                            daysOfWeek = daysOfWeek,
                            startTime = startTime,
                            endTime = endTime,
                            location = location,
                            description = description
                        )
                        event

                    } catch (e: Exception) {
                        // Log the error or handle it as needed
                        Log.w("Firestore", "in the catch block some error was fount ${e}")
                        null
                    }
                }
                Log.d("Firestore", "Events Must be retrieved")
                EventRepository.events.clear()
                EventRepository.events.addAll(events)
                callback(events)
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                println("Error getting documents: $exception")
                callback(emptyList())
            }
    }


    private fun parseTimeObject(timeObject: Map<String, Any>?): LocalTime? {
        return try {
            val hour = (timeObject?.get("hour") as? Long)?.toInt() ?: return null
            val minute = (timeObject?.get("minute") as? Long)?.toInt() ?: return null
            LocalTime.of(hour, minute)
        } catch (e: Exception) {
            println("Error parsing time object: ${e.message}")
            null
        }
    }

    fun incrementCounter(){
        counter++
    }

    fun getEventsForWeek(): ArrayList<Event> {
        // In a real app, you would filter by the current week's dates
        return events
    }

    fun addEvent(event: Event, callback: (Boolean) -> Unit) {
        if (doesOverlap(event)) {
            // cannot add the event
            callback(false)
            return
        }

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: run {
            callback(false)
            return
        }

        db.collection("users").document(userId).collection("events")
            .document(event.id.toString())  // Use the event's ID as the document ID
            .set(event)
            .addOnSuccessListener {
                Log.d("Firestore", "Event successfully added")
                events.add(event)
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding event", e)
                callback(false)
            }
    }

    //    overlapping needs to be checked
    fun updateEvent(event: Event, callback: (Boolean) -> Unit) {
        val index = events.indexOfFirst { it.id == event.id }

        if (index == -1) {
            callback(false)
            return
        }

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: run {
            callback(false)
            return
        }

        db.collection("users").document(userId).collection("events")
            .document(event.id.toString())  // Use the event's ID as the document ID
            .set(event)  // Use set() instead of add()
            .addOnSuccessListener {
                Log.d("Firestore", "Event successfully updated")
                events[index] = event
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating event", e)
                callback(false)
            }
    }


    fun deleteEvent(event: Event, callback: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: run {
            callback(false)
            return
        }

        db.collection("users").document(userId).collection("events")
            .document(event.id.toString())  // Use the event's ID as the document ID
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Event successfully deleted")
                events.remove(event)
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error deleting event", e)
                callback(false)
            }
    }


    fun doesOverlap(event: Event): Boolean {
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