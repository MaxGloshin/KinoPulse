package ru.openstream.kinopulse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.compose.material3.Button
import kotlinx.coroutines.delay
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import ru.openstream.kinopulse.domain.model.StreamVariant

@Composable
fun VideoPlayerScreen(
    title: String,
    variant: StreamVariant,
    initialPositionMs: Long,
    onSavePosition: (Long) -> Unit,
    onCloseWithPosition: (Long) -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember(variant.streamUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(variant.streamUrl))
            prepare()
            if (initialPositionMs > 0L) {
                seekTo(initialPositionMs)
            }
            playWhenReady = true
        }
    }

    val savePosition by rememberUpdatedState(onSavePosition)
    val lifecycleOwner = LocalLifecycleOwner.current
    var error by remember { mutableStateOf<String?>(null) }
    BackHandler { onCloseWithPosition(exoPlayer.currentPosition) }
    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(5000)
            savePosition(exoPlayer.currentPosition)
        }
    }
    DisposableEffect(exoPlayer, lifecycleOwner) {
        val listener = object : Player.Listener {
            override fun onPlayerError(exception: PlaybackException) {
                error = "Не удалось открыть видео. Проверьте подключение и повторите попытку."
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                savePosition(exoPlayer.currentPosition)
                exoPlayer.pause()
            }
        }
        exoPlayer.addListener(listener)
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onCloseWithPosition(exoPlayer.currentPosition) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Закрыть плеер",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Источник: ${variant.sourceName} • ${variant.quality} • ${variant.audioTrack}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        error?.let { message ->
            Text(message, color = Color.White, modifier = Modifier.padding(16.dp))
            Button(onClick = { error = null; exoPlayer.prepare(); exoPlayer.play() }) {
                Text("Повторить")
            }
        }
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
