package com.example.calenderapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.calenderapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import java.time.DayOfWeek
import java.time.LocalTime
import android.Manifest


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var eventAdapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize adapter with events from repository
        eventAdapter = EventAdapter(this, EventRepository.getEventsForWeek())
        binding.eventListView.adapter = eventAdapter

        val logoutBtn = binding.logoutBtn
        val modifyScheduleBtn = binding.modBtn

        fetchEventsAndUpdateUI()
        eventAdapter.notifyDataSetChanged()

        auth = FirebaseAuth.getInstance()
        createNotificationChannel()
        requestNotificationPermission()

        val user = auth.currentUser
        Log.d("FIREBASE USERNAME FROM MAINACTIVITY", "${user?.email}")

        if (user == null){
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


        logoutBtn.setOnClickListener{logoutBtinClicked()}
        modifyScheduleBtn.setOnClickListener{
            modBtnClicked()
        }

    }

    override fun onResume() {
        super.onResume()
        fetchEventsAndUpdateUI()
        eventAdapter.notifyDataSetChanged()
    }


    private fun createNotificationChannel() {
        val name = "Event Reminders"
        val descriptionText = "Channel for event reminder notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("EVENT_REMINDER_CHANNEL", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun modBtnClicked() {
        val intent = Intent(applicationContext, EventForm::class.java)
        startActivity(intent)
        finish()
    }

    private fun logoutBtinClicked() {
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun fetchEventsAndUpdateUI() {
        EventRepository.getEventsFromFirebase(this) { events ->
            runOnUiThread {
                if (events.isNotEmpty()) {
                    Log.d("MainActivity", "Retrieving Events From Firebase")
//                    EventRepository.events.clear()
//                    EventRepository.events.addAll(events)
//                    eventAdapter.clear()
//                    eventAdapter.addAll(events)
                    eventAdapter.notifyDataSetChanged()
                } else {
                    Log.d("MainActivity", "No events found")
                }
            }
        }
    }


    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }



}