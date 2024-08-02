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

//    1) create a list view
//    2) In the list add a delete button to delete event
//    3) Have an update functionality by either doing it in place or a new activity.

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

        eventAdapter.notifyDataSetChanged()
        updateDisplay()

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser


        if (user == null){
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        logoutBtn.setOnClickListener{logoutBtinClicked()}
        modifyScheduleBtn.setOnClickListener{modBtnClicked()}

    }

    override fun onResume() {
        super.onResume()
        eventAdapter.notifyDataSetChanged()
        updateDisplay()
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
//        val eventsText = binding.tvEventsList

//        counterText.text = EventRepository.counter.toString()
//        eventsText.text = EventRepository.events.toString()
    }
}