package com.aymen.handwritinglab

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object DatasetStore {
    fun saveSample(context: Context, category: String, character: String, sample: Int, strokes: List<Stroke>) {
        val safeCategory = category.lowercase().replace("[^a-z0-9]+".toRegex(), "_")
        val safeCharacter = character.codePoints().toArray().joinToString("_")
        val dir = File(context.filesDir, "handwriting/$safeCategory/$safeCharacter").apply { mkdirs() }
        val json = JSONObject().apply {
            put("category", category)
            put("character", character)
            put("sample", sample)
            put("canvasWidth", 0)
            put("canvasHeight", 0)
            put("strokes", JSONArray().apply {
                strokes.forEach { stroke ->
                    put(JSONArray().apply {
                        stroke.points.forEach { p ->
                            put(JSONObject().apply {
                                put("x", p.x)
                                put("y", p.y)
                                put("pressure", p.pressure)
                                put("time", p.time)
                            })
                        }
                    })
                }
            })
        }
        File(dir, "%03d.json".format(sample)).writeText(json.toString(2))
    }
}
