package com.aymen.handwritinglab

object SvgExporter {
    fun toSvg(strokes: List<Stroke>, width: Float, height: Float): String = buildString {
        append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"$width\" height=\"$height\" viewBox=\"0 0 $width $height\">\n")
        append("  <g fill=\"none\" stroke=\"black\" stroke-width=\"6\" stroke-linecap=\"round\" stroke-linejoin=\"round\">\n")
        strokes.forEach { stroke ->
            if (stroke.points.isEmpty()) return@forEach
            append("    <path d=\"M ${stroke.points.first().x} ${stroke.points.first().y}")
            stroke.points.drop(1).forEach { p -> append(" L ${p.x} ${p.y}") }
            append("\"/>\n")
        }
        append("  </g>\n</svg>\n")
    }
}
