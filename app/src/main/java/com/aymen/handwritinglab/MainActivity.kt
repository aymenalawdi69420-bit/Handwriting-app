package com.aymen.handwritinglab

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { HandwritingLabScreen() } }
    }
}

private val uppercase = ('A'..'Z').map(Char::toString)
private val lowercase = ('a'..'z').map(Char::toString)
private val numbers = ('0'..'9').map(Char::toString)
private val punctuation = listOf(".", ",", "?", "!", ":", ";", "'", "\"", "(", ")", "[", "]", "-", "_", "/", "\\", "+", "=", "*", "%", "&", "@", "#", "$", "π", "×", "÷", "±", "√", "≤", "≥", "≠", "∞", "°")

@Composable
private fun HandwritingLabScreen() {
    val context = LocalContext.current
    var category by remember { mutableStateOf("Uppercase") }
    var index by remember { mutableIntStateOf(0) }
    var sample by remember { mutableIntStateOf(1) }
    var strokes by remember { mutableStateOf(emptyList<Stroke>()) }
    var canvasView by remember { mutableStateOf<HandwritingView?>(null) }
    var status by remember { mutableStateOf("") }
    var showReview by remember { mutableStateOf(false) }

    if (showReview) {
        ReviewScreen(context, onBack = { showReview = false }, onExport = { file ->
            val uri = androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "Export handwriting dataset"))
        })
        return
    }

    val characters = when (category) {
        "Uppercase" -> uppercase
        "Lowercase" -> lowercase
        "Numbers" -> numbers
        else -> punctuation
    }
    val character = characters[index]

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("HANDWRITING LAB", style = MaterialTheme.typography.headlineMedium)
        Text("Capture your natural handwriting with the S Pen")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Uppercase", "Lowercase", "Numbers", "Symbols").forEach { name ->
                OutlinedButton(onClick = { category = name; index = 0; sample = 1; strokes = emptyList(); canvasView?.clear() }) { Text(name) }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Write:", style = MaterialTheme.typography.titleMedium)
        Text(character, style = MaterialTheme.typography.displayLarge)
        Text("Sample $sample / 15")
        Spacer(Modifier.height(8.dp))
        AndroidView(modifier = Modifier.fillMaxWidth().weight(1f), factory = { ctx ->
            HandwritingView(ctx).also { view -> canvasView = view; view.onStrokesChanged = { strokes = it } }
        }, update = { view -> canvasView = view; view.onStrokesChanged = { strokes = it } })
        Spacer(Modifier.height(8.dp))
        if (status.isNotEmpty()) Text(status)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { canvasView?.clear() }) { Text("Clear") }
            OutlinedButton(onClick = { showReview = true }) { Text("Review / Export") }
            Button(onClick = {
                if (strokes.isEmpty()) return@Button
                DatasetStore.saveSample(context, category, character, sample, strokes)
                status = "Saved $character sample $sample"
                if (sample < 15) sample++ else { sample = 1; index = (index + 1) % characters.size }
                canvasView?.clear(); strokes = emptyList()
            }) { Text(if (sample < 15) "Save & Next" else "Save & Next Character") }
        }
    }
}

@Composable
private fun ReviewScreen(context: Context, onBack: () -> Unit, onExport: (java.io.File) -> Unit) {
    val root = java.io.File(context.filesDir, "handwriting")
    val files = if (root.exists()) root.walkTopDown().count { it.isFile && it.extension == "json" } else 0
    val chars = if (root.exists()) root.walkTopDown().filter { it.isFile && it.extension == "json" }.mapNotNull { it.parentFile?.name }.toSet().size else 0
    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("YOUR HANDWRITING DATA", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("$files samples saved across $chars characters")
        Spacer(Modifier.height(20.dp))
        Button(onClick = { onExport(DatasetStore.exportZip(context)) }, enabled = files > 0) { Text("Export Dataset ZIP") }
        Spacer(Modifier.height(8.dp))
        Text("The ZIP contains labeled JSON stroke data and an SVG for every saved sample.")
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onBack) { Text("Back to Practice") }
    }
}
