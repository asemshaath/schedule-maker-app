package com.example.calenderapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import java.time.DayOfWeek
import java.time.LocalTime

class WeeklyCalendarView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 40f
    }
    private val rect = Rect()
    var events: List<Event> = emptyList()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGrid(canvas)
        drawEvents(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        paint.color = Color.LTGRAY
        paint.strokeWidth = 2f

        val dayWidth = width / 7
        val hourHeight = height / 24

        // Draw vertical lines for days
        for (i in 1..6) {
            val x = i * dayWidth
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), paint)
        }

        // Draw horizontal lines for hours
        for (i in 1..23) {
            val y = i * hourHeight
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        }
    }

    private fun drawEvents(canvas: Canvas) {
        val dayWidth = width / 7
        val hourHeight = height / 24

        for (event in events) {
            for (day in event.daysOfWeek) {
                val startHour = event.startTime.hour
                val endHour = event.endTime.hour

                val left = (day.ordinal * dayWidth).toFloat()
                val top = (startHour * hourHeight).toFloat()
                val right = left + dayWidth
                val bottom = (endHour * hourHeight).toFloat()

                paint.color = event.color
                canvas.drawRect(left, top, right, bottom, paint)

                // Draw event title
                textPaint.getTextBounds(event.title, 0, event.title.length, rect)
                val textX = left + 10
                val textY = top + rect.height() + 10
                canvas.drawText(event.title, textX, textY, textPaint)
            }
        }
    }
}
