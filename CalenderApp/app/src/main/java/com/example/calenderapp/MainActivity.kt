package com.example.calenderapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.calenderapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import java.time.DayOfWeek
import java.time.LocalTime

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    lateinit var weeklyCalendarView: WeeklyCalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val logoutBtn = binding.logoutBtn
        val modifyScheduleBtn = binding.modBtn
        val userDetails = binding.userDetails
        weeklyCalendarView = binding.weeklyCalendarView

        EventRepository.addEvent(
            Event(1, "Meeting", listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), LocalTime.of(9, 0), LocalTime.of(10, 0), "Room A", "Team meeting", Color.RED),
        )
        EventRepository.addEvent(
            Event(2, "Workshop", listOf(DayOfWeek.TUESDAY), LocalTime.of(13, 0), LocalTime.of(15, 0), "Room B", "Android Workshop", Color.BLUE)
        )

        weeklyCalendarView.events = EventRepository.events
        weeklyCalendarView.invalidate() // Refresh the view
        updateDisplay()

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser


        if (user == null){
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            userDetails.text = user.email
        }

        logoutBtn.setOnClickListener{logoutBtinClicked()}
        modifyScheduleBtn.setOnClickListener{modBtnClicked()}

    }

    override fun onResume() {
        super.onResume()
        updateDisplay()
        weeklyCalendarView.events = EventRepository.events
        weeklyCalendarView.invalidate() // Refresh the view
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

    private fun updateDisplay(){
        val counterText = binding.counter
//        val eventsText = binding.tvEventsList

        counterText.text = EventRepository.counter.toString()
//        eventsText.text = EventRepository.events.toString()
    }
}