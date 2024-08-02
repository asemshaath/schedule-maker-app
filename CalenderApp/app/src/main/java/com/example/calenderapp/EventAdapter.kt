package com.example.calenderapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

        // Use binding to set text and click listeners
        binding.eventTitle.text = event.title
        binding.eventLocation.text = event.location

        binding.updateButton.setOnClickListener {
            showUpdateDialog(event)
        }

        binding.deleteButton.setOnClickListener {
            EventRepository.deleteEvent(event)
            notifyDataSetChanged()
        }

        binding.root.setOnClickListener {
            showDetailsDialog(event)
        }

        return binding.root
    }

    private fun showUpdateDialog(event: Event) {
        val dialogBinding = DialogUpdateEventBinding.inflate(LayoutInflater.from(context))

        dialogBinding.titleEditText.setText(event.title)
        dialogBinding.locationEditText.setText(event.location)
        // Set other fields as necessary

        AlertDialog.Builder(context)
            .setTitle("Update Event")
            .setView(dialogBinding.root)
            .setPositiveButton("Update") { _, _ ->
                // Update the event object
                event.title = dialogBinding.titleEditText.text.toString()
                event.location = dialogBinding.locationEditText.text.toString()
                // Update other fields

                if (EventRepository.doesOverlap(event)) {
                    Toast.makeText(context, "Event overlaps with existing events", Toast.LENGTH_SHORT).show()
                } else {
                    EventRepository.updateEvent(event)
                    notifyDataSetChanged()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
