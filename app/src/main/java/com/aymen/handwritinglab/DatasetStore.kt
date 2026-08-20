package com.aymen.handwritinglab

import android.content.Context
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.json.JSONArray
import org.json.JSONObject

object DatasetStore {
    fun saveSample(context: Context, category: String, character: String, sample: Int, strokes: List<Stroke>) {
        val safeCategory = category.lowercase().replace("[^a-z0-9]+".toRegex(), "_")
        val safeCharacter = character.codePoints().toArray().joinToString("_")
        val dir = File(context.filesDir, "handwriting/$safeCategory/$safeCharacter").apply { mkdirs() }
        val json = JSONObject().apply {
            put("category", category)
            put("character", character)
            put("sample", sample)
            put("strokes", JSONArray().apply {
                strokes.forEach { stroke ->
                    put(JSONArray().apply {
                        stroke.points.forEach { p ->
                            put(JSONObject().apply {
                                put("x", p.x); put("y", p.y); put("pressure", p.pressure); put("time", p.time)
                            })
                        }
                    })
                }
            })
        }
        File(dir, "%03d.json".format(sample)).writeText(json.toString(2))
        File(dir, "%03d.svg".format(sample)).writeText(toSvg(character, strokes))
    }

    fun exportZip(context: Context): File {
        val source = File(context.filesDir, "handwriting")
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val zip = File(exportDir, "Aymen_Handwriting.zip")
        ZipOutputStream(zip.outputStream().buffered()).use { out ->
            if (source.exists()) source.walkTopDown().filter { it.isFile }.forEach { file ->
                val name = "handwriting/" + file.relativeTo(source).invariantSeparatorsPath
                out.putNextEntry(ZipEntry(name)); file.inputStream().use { it.copyTo(out) }; out.closeEntry()
            }
        }
        return zip
    }

    private fun toSvg(character: String, strokes: List<Stroke>): String {
        val points = strokes.flatMap { it.points }
        val maxX = maxOf(1f, points.maxOfOrNull { it.x } ?: 1f)
        val maxY = maxOf(1f, points.maxOfOrNull { it.y } ?: 1f)
        val paths = strokes.joinToString("\n") { stroke ->
            if (stroke.points.isEmpty()) "" else {
                val d = buildString {
                    append("M "); append(stroke.points.first().x); append(' '); append(stroke.points.first().y)
                    stroke.points.drop(1).forEach { append(" L "); append(it.x); append(' '); append(it.y) }
                }
                "<path d=\"$d\" fill=\"none\" stroke=\"black\" stroke-width=6 stroke-linecap=\"round\" stroke-linejoin=\"round\"/>"
            }
        }
        return """<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="$maxX" height="$maxY" viewBox="0 0 $maxX $maxY">
<metadata>character=${escapeXml(character)}</metadata>
$paths
</svg>
"""
    }

    private fun escapeXml(value: String) = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
}
