package com.example.calenderapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.calenderapp.databinding.ActivityEventFormBinding
import com.google.android.material.chip.Chip
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class EventForm : AppCompatActivity() {

    private lateinit var binding: ActivityEventFormBinding
//    val schedule = EventRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val addEventBtn = binding.addEventBtn
        val viewSchBtn = binding.viewSchBtn

        addEventBtn.setOnClickListener { addEvent() }
        viewSchBtn.setOnClickListener { viewSch() }
    }

    private fun viewSch() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun isTimeEmpty(): Boolean{
        return binding.etStartTimeP1.text.toString().isEmpty() ||
                binding.etStartTimeP2.text.toString().isEmpty() ||
                binding.etEndTimeP1.text.toString().isEmpty() ||
                binding.etEndTimeP2.text.toString().isEmpty()
    }

    private fun isTimeValid(): Boolean{
        return binding.etStartTimeP1.text.toString().toInt() > 12 ||
                binding.etStartTimeP2.text.toString().toInt() > 59 ||
                binding.etEndTimeP1.text.toString().toInt() > 12 ||
                binding.etEndTimeP2.text.toString().toInt() > 59
    }

    private fun getDaysOfWeek(): List<DayOfWeek>{
        val daysSelected = binding.cgDaysChecked.checkedChipIds.map{
                chipId ->
            binding.cgDaysChecked.findViewById<Chip>(chipId).text.toString()
        }

        return daysSelected.map { dayString ->
            DayOfWeek.valueOf(dayString.toUpperCase())
        }
    }

    private fun getRandomColor(): Int {
        val red = Random.nextInt(256)
        val green = Random.nextInt(256)
        val blue = Random.nextInt(256)
        return (red shl 16) or (green shl 8) or blue
    }


    private fun addEvent() {
        val title = binding.etTitle.text.toString()
        val des = binding.etDescribtion.text.toString()
        val location = binding.etLocation.text.toString()
        val id = EventRepository.generateId()
        val blockColor = getRandomColor()
        val daysSelected = getDaysOfWeek()

        Log.i("TEST ADD EVENT", daysSelected.toString())

        if (title.isEmpty() || location.isEmpty() || isTimeEmpty()) {
            Toast.makeText(this, "Title, location, or time might be empty!",Toast.LENGTH_SHORT).show()
            return
        }

        if (isTimeValid()){
            Toast.makeText(this, "Enter a valid time in 12-hour format",Toast.LENGTH_SHORT).show()
            return
        }

        val formatter = DateTimeFormatter.ofPattern("h:mm a")

        val startTimeStr  = binding.etStartTimeP1.text.toString() +  ":" +  binding.etStartTimeP2.text.toString() + " " + binding.StartTimeToggle.text
        var endTimeStr  = binding.etEndTimeP1.text.toString() +  ":" +  binding.etEndTimeP2.text.toString() + " " + binding.StartTimeToggle.text

        val startTime = LocalTime.parse(startTimeStr, formatter)
        val endTime = LocalTime.parse(endTimeStr, formatter)



        Log.d("TEST", "Title is:$title")
        Log.d("TEST", "Start time is $startTime")
        println("try printing something")
        print(binding.etTitle)
        print(startTime)
        EventRepository.incrementCounter()

        // add event to the repo
        val event = Event(id, title, daysSelected, startTime, endTime, location, des, blockColor)
        EventRepository.addEvent(event)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}