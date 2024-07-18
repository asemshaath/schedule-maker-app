package com.example.calenderapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.calenderapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val logoutBtn = binding.logoutBtn
        val modifyScheduleBtn = binding.modBtn
        val userDetails = binding.userDetails

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
        val eventsText = binding.tvEventsList

        counterText.text = EventRepository.counter.toString()
        eventsText.text = EventRepository.events.toString()
    }
}