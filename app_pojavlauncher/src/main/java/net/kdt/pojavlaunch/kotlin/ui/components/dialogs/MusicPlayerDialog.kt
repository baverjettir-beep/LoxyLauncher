package net.kdt.pojavlaunch.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.kdt.pojavlaunch.utils.MusicPlayerManager

@Composable
fun MusicPlayerDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val tracks = MusicPlayerManager.tracks
    val current = MusicPlayerManager.currentIndex

    LaunchedEffect(Unit) { MusicPlayerManager.load(context) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNullOrEmpty()) return@rememberLauncherForActivityResult
        uris.forEach {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
            }
        }
        MusicPlayerManager.addTracks(context, uris)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Music") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (current in tracks.indices) tracks[current].title else "Nothing playing",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { MusicPlayerManager.previous(context) }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                    }
                    IconButton(onClick = { MusicPlayerManager.toggle(context) }) {
                        Icon(
                            if (MusicPlayerManager.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play / Pause"
                        )
                    }
                    IconButton(onClick = { MusicPlayerManager.next(context) }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next")
                    }
                }
                HorizontalDivider()
                if (tracks.isEmpty()) {
                    Text(
                        text = "No songs yet. Tap \"Add songs\" to pick audio files from your device.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                        itemsIndexed(tracks) { index, track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { MusicPlayerManager.play(context, index) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (index == current) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = track.title,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(
                                    onClick = { MusicPlayerManager.removeTrack(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { picker.launch(arrayOf("audio/*")) }) { Text("Add songs") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
