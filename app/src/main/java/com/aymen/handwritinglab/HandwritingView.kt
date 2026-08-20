package com.aymen.handwritinglab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class HandwritingView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val paths = mutableListOf<Path>()
    private val currentPoints = mutableListOf<StrokePoint>()
    private val allStrokes = mutableListOf<Stroke>()

    var onStrokesChanged: ((List<Stroke>) -> Unit)? = null

    init {
        setBackgroundColor(android.graphics.Color.WHITE)
        isFocusable = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paths.forEach { canvas.drawPath(it, paint) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = min(width.toFloat(), max(0f, event.x))
        val y = min(height.toFloat(), max(0f, event.y))
        val pressure = if (event.pressure > 0f) event.pressure else 1f

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentPoints.clear()
                currentPoints += StrokePoint(x, y, pressure, event.eventTime)
                paths += Path().apply { moveTo(x, y) }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPoints += StrokePoint(x, y, pressure, event.eventTime)
                paths.lastOrNull()?.lineTo(x, y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                currentPoints += StrokePoint(x, y, pressure, event.eventTime)
                if (currentPoints.size > 1) allStrokes += Stroke(currentPoints.toList())
                currentPoints.clear()
                onStrokesChanged?.invoke(allStrokes.toList())
                invalidate()
                return true
            }
        }
        return true
    }

    fun clear() {
        paths.clear()
        allStrokes.clear()
        currentPoints.clear()
        onStrokesChanged?.invoke(emptyList())
        invalidate()
    }
}
