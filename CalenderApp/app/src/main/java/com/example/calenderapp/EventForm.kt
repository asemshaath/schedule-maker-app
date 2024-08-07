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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val addEventBtn = binding.addEventBtn
        val viewSchBtn = binding.viewSchBtn

        val eventId = intent.extras?.getInt("eventId")
        Log.d("EVENT ID", "$eventId")

        eventId?.let {
            // Load event details from database or repository
            val e = EventRepository.getEvenById(eventId)

            e?.let {
                populateFormWithEventData(it)
            }
        }


        addEventBtn.setOnClickListener {

            addEvent(eventId)
//            if (eventId == null)
//                addEvent()
//            else
//                editEvent()
        }
        viewSchBtn.setOnClickListener { viewSch() }
    }


    private fun populateFormWithEventData(event: Event) {
//        val id: Int, // could be used by the size of the repository
//        var daysOfWeek: List<DayOfWeek>,

        // Adding strings
        binding.etTitle.setText(event.title)
        binding.etLocation.setText(event.location)
        binding.etDescribtion.setText(event.description)

        // Add time
        binding.etStartTimeP1.setText(to12hrFormat(event.startTime.hour).toString())
        binding.etStartTimeP2.setText(event.startTime.minute.toString())
        binding.StartTimeToggle.setChecked(let { if (event.startTime.hour > 12) false else true })

        binding.etEndTimeP1.setText(to12hrFormat(event.endTime.hour).toString())
        binding.etEndTimeP2.setText(event.endTime.minute.toString())
        binding.EndTimeToggle.setChecked(let { if (event.endTime.hour > 12) false else true })

        // Add days

        // Edit layout
        binding.addEventBtn.text = "Edit Event"
        binding.tvEventForm.text = "Editing ${event.title}"
    }

    private fun to12hrFormat(hr: Int):Int {
        if(hr > 12)
            return hr - 12
        return hr
    }

    private fun viewSch() {
        // Will go to the main activity
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

    private fun addEvent(eventId: Int?) {
        val title = binding.etTitle.text.toString()
        val des = binding.etDescribtion.text.toString()
        val location = binding.etLocation.text.toString()
        val id = let { if (eventId == null) EventRepository.generateId() else eventId}
        val daysSelected = getDaysOfWeek()
//        val meth

        Log.i("TEST ADD EVENT", daysSelected.toString())

        if (title.isEmpty() || location.isEmpty() || isTimeEmpty() || daysSelected.isEmpty()) {
            Toast.makeText(this, "Title, location, or time might be empty!",Toast.LENGTH_SHORT).show()
            return
        }

        if (isTimeValid()){
            Toast.makeText(this, "Enter a valid time in 12-hour format",Toast.LENGTH_SHORT).show()
            return
        }

        val formatter = DateTimeFormatter.ofPattern("h:mm a")

        val startTimeStr  = binding.etStartTimeP1.text.toString() + ":" + "%02d".format(binding.etStartTimeP2.text.toString().toIntOrNull() ?: 0) + " " + binding.StartTimeToggle.text
        val endTimeStr  = binding.etEndTimeP1.text.toString() + ":" + "%02d".format(binding.etEndTimeP2.text.toString().toIntOrNull() ?: 0) + " " + binding.EndTimeToggle.text

        val startTime = LocalTime.parse(startTimeStr, formatter)
        val endTime = LocalTime.parse(endTimeStr, formatter)

        Log.d("TIME TEST", "Start Time User Input: $startTimeStr")
        Log.d("TIME TEST", "Start Time Localtime: $startTime")

        Log.d("TIME TEST", "End Time User Input: $endTimeStr")
        Log.d("TIME TEST", "End Time Localtime: $endTime")

//        EventRepository.incrementCounter()

        // add event to the repo
        val event = Event(id, title, daysSelected, startTime, endTime, location, des)

        if(EventRepository.addEvent(event)){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        } else Toast.makeText(this, "Events Cannot Overlap",Toast.LENGTH_SHORT).show()


    }

    private fun editEvent() {
        Log.d("EDIT EVENT", "EDIT EVENT TRIGGERED")
    }

}