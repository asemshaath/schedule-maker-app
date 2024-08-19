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

    fun scheduleNotificationsForEvent(context: Context, event: Event) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (day in event.daysOfWeek) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, day.value)
                set(Calendar.HOUR_OF_DAY, event.startTime.hour)
                set(Calendar.MINUTE, event.startTime.minute - 15) // 15 minutes before
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // If the time has already passed this week, move to next week
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }

            val intent = Intent(context, Notification::class.java).apply {
                putExtra(titleExtra, event.title)
                putExtra(messageExtra, "${event.title} will start in 15 minutes")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                event.id * 10 + day.value,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7, // Repeat weekly
                pendingIntent
            )

            Log.d(
                "EventNotification",
                "Scheduled weekly notification for ${event.title} on ${day.name} at ${event.startTime}, first trigger at ${calendar.time}"
            )
        }
    }

    private fun calculateNotificationTime(day: DayOfWeek, startTime: LocalTime): Long {
        val calendar = Calendar.getInstance()

        // Set the day of week
        calendar.set(Calendar.DAY_OF_WEEK, day.value)

        calendar.set(Calendar.HOUR_OF_DAY, startTime.hour)
        calendar.set(Calendar.MINUTE, startTime.minute)
        calendar.set(Calendar.SECOND, 0)

        // If the calculated time is in the past, add 7 days
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 7)
        }

        return calendar.timeInMillis
    }

    fun cancelNotificationsForEvent(context: Context, event: Event) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (day in event.daysOfWeek) {
            val intent = Intent(context, Notification::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                event.id * 10 + day.value,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
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

        db.collection("events").document(userId).collection("my_event")
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
                        scheduleNotificationsForEvent(context, event)
                        event

                    } catch (e: Exception) {
                        // Log the error or handle it as needed
                        Log.w("Firestore", "in the catch block some error was fount ${e}")
                        null
                    }
                }
                Log.d("Firestore", "Events Must be retrieved")
                clearAllNotifications(context)
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

    private fun clearAllNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        events.forEach { event ->
            event.daysOfWeek.forEach { day ->
                val intent = Intent(context, Notification::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    event.id * 10 + day.value,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
                )
                pendingIntent?.let {
                    alarmManager.cancel(it)
                    it.cancel()
                }
            }
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

    fun addEvent(context: Context, event: Event): Boolean {

        if (doesOverlap(event)){
            // cannot add the event
            return false
        }

        events.add(event)
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return false

        db.collection("events").document(userId).collection("my_event")
            .document(event.id.toString())  // Use the event's ID as the document ID
            .set(event)  // Use set() instead of add()
            .addOnSuccessListener {
                Log.d("Firestore", "Event successfully added")
                scheduleNotificationsForEvent(context, event)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding event", e)
            }

        return true // added event successfully
    }


//    overlapping needs to be checked
    fun updateEvent(context: Context, event: Event): Boolean {
        val index = events.indexOfFirst { it.id == event.id }
        if (index != -1) {
            events[index] = event

            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid ?: return false

            db.collection("events").document(userId).collection("my_event")
                .document(event.id.toString())  // Use the event's ID as the document ID
                .set(event)  // Use set() instead of add()
                .addOnSuccessListener {
                    Log.d("Firestore", "Event successfully updated")
                    cancelNotificationsForEvent(context, events[index])
                    scheduleNotificationsForEvent(context, event)
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error updating event", e)
                }

            return true
        }
        return false
    }

    fun deleteEvent(context: Context, event: Event): Boolean {

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return false

        db.collection("events").document(userId).collection("my_event")
            .document(event.id.toString())  // Use the event's ID as the document ID
            .delete()  // Use set() instead of add()
            .addOnSuccessListener {
                Log.d("Firestore", "Event successfully deleted")
                cancelNotificationsForEvent(context, event)
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error deleting event", e)
            }

        events.remove(event)

        return true
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