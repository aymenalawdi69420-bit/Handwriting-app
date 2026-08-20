package com.aymen.handwritinglab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HandwritingLabScreen()
            }
        }
    }
}

private val uppercase = ('A'..'Z').map(Char::toString)
private val lowercase = ('a'..'z').map(Char::toString)
private val numbers = ('0'..'9').map(Char::toString)
private val punctuation = listOf(".", ",", "?", "!", ":", ";", "'", "\"", "(", ")", "[", "]", "-", "_", "/", "\\", "+", "=", "*", "%", "&", "@", "#", "$", "π", "×", "÷", "±", "√", "≤", "≥", "≠", "∞", "°")

@androidx.compose.runtime.Composable
private fun HandwritingLabScreen() {
    var category by remember { mutableStateOf("Uppercase") }
    var index by remember { mutableIntStateOf(0) }
    var sample by remember { mutableIntStateOf(1) }
    var strokes by remember { mutableStateOf(emptyList<Stroke>()) }

    val characters = when (category) {
        "Uppercase" -> uppercase
        "Lowercase" -> lowercase
        "Numbers" -> numbers
        else -> punctuation
    }
    val character = characters[index]

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("HANDWRITING LAB", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Capture your natural handwriting with the S Pen")
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Uppercase", "Lowercase", "Numbers", "Symbols").forEach { name ->
                OutlinedButton(onClick = {
                    category = name
                    index = 0
                    sample = 1
                    strokes = emptyList()
                }) { Text(name) }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Write:", style = MaterialTheme.typography.titleMedium)
        Text(character, style = MaterialTheme.typography.displayLarge)
        Text("Sample $sample / 15")
        Spacer(Modifier.height(12.dp))

        AndroidView(
            modifier = Modifier.fillMaxWidth().weight(1f),
            factory = { context ->
                HandwritingView(context).also { view ->
                    view.onStrokesChanged = { strokes = it }
                }
            },
            update = { it.onStrokesChanged = { strokes = it } }
        )

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { strokes = emptyList() }) { Text("Clear") }
            Button(onClick = {
                if (strokes.isEmpty()) return@Button
                DatasetStore.saveSample(this@HandwritingLabScreen, category, character, sample, strokes)
                if (sample < 15) sample++ else {
                    sample = 1
                    index = (index + 1) % characters.size
                }
                strokes = emptyList()
            }) { Text(if (sample < 15) "Save & Next" else "Save & Next Character") }
        }
    }
}
