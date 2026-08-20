package com.aymen.handwritinglab

data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float,
    val time: Long
)

data class Stroke(val points: List<StrokePoint>)
