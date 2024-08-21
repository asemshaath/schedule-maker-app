package com.example.calenderapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import com.example.calenderapp.databinding.DialogUpdateEventBinding
import com.example.calenderapp.databinding.EventListItemBinding

class EventAdapter(
    private val context: Context,
    private val events: ArrayList<Event>
) : BaseAdapter() {

    override fun getCount(): Int = events.size

    override fun getItem(position: Int): Event = events[position]

    override fun getItemId(position: Int): Long = events[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: EventListItemBinding = if (convertView == null) {
            EventListItemBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            EventListItemBinding.bind(convertView)
        }

        val event = getItem(position)

        val dayAbbreviations = mapOf(
            "monday" to "M",
            "tuesday" to "T",
            "wednesday" to "W",
            "thursday" to "R",
            "friday" to "F",
            "saturday" to "S",
            "sunday" to "U"
        )

        val days = event.daysOfWeek.map { day ->

            dayAbbreviations[day.toString().toLowerCase()] ?: ""  // Use the abbreviation, or "" if day is not found
        }.joinToString("")

        // Use binding to set text and click listeners
        binding.eventTitle.text = event.title
        binding.eventLocation.text = "Location: ${event.location}"
        binding.eventTime.text = "$days  ${event.startTime} - ${event.endTime}"

        binding.updateButton.setOnClickListener {
//            showUpdateDialog(event)

            val intent = Intent(context, EventForm::class.java)
            intent.putExtra("eventId", event.id)
            context.startActivity(intent) // Make sure context is an Activity context or use context.startActivity(intent, null)

        }

        binding.deleteButton.setOnClickListener {

            EventRepository.deleteEvent(event) { success ->
                if (success) {
                    // Event was deleted successfully
                    events.remove(event)
                    notifyDataSetChanged()
                    Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_SHORT).show()

                } else {
                    // Failed to delete event
                    Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show()

                }
            }

        }

        binding.root.setOnClickListener {
            showDetailsDialog(event)
        }

        return binding.root
    }


    private fun showDetailsDialog(event: Event) {
        AlertDialog.Builder(context)
            .setTitle(event.title)
            .setMessage(
                "Location: ${event.location}\nDays: ${
                    event.daysOfWeek.joinToString()
                }\nTime: ${event.startTime} - ${event.endTime}\nDescription: ${event.description}"
            )
            .setPositiveButton("OK", null)
            .show()
    }
}
