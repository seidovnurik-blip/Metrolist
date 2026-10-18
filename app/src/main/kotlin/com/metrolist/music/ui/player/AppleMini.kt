package com.metrolist.music.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.metrolist.music.R
import com.metrolist.music.listentogether.ListenTogetherManager
import com.metrolist.music.models.MediaMetadata
import com.metrolist.music.playback.CastConnectionHandler
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.ui.utils.resize

@Composable
fun AppleMiniCover(mediaMetadata: MediaMetadata?) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF3A3A3C)),
    ) {
        mediaMetadata?.let { metadata ->
            val thumbnailUrl = remember(metadata.thumbnailUrl) { metadata.thumbnailUrl?.resize(120, 120) }
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun AppleMiniControls(
    playbackState: Int,
    isCasting: Boolean,
    castHandler: CastConnectionHandler?,
    playerConnection: PlayerConnection,
    listenTogetherManager: ListenTogetherManager?,
    tint: Color,
) {
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val castIsPlaying by castHandler?.castIsPlaying?.collectAsState() ?: remember { mutableStateOf(false) }
    val effectiveIsPlaying = if (isCasting) castIsPlaying else isPlaying
    val isListenTogetherGuest = listenTogetherManager?.let { it.isInRoom && !it.isHost } ?: false
    val isMuted by playerConnection.isMuted.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    val iconRes =
        when {
            isListenTogetherGuest -> if (isMuted) R.drawable.volume_off else R.drawable.volume_up
            playbackState == Player.STATE_ENDED -> R.drawable.replay
            effectiveIsPlaying -> R.drawable.pause
            else -> R.drawable.play
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (isListenTogetherGuest) {
                            playerConnection.toggleMute()
                        } else if (isCasting) {
                            if (castIsPlaying) castHandler?.pause() else castHandler?.play()
                        } else if (playbackState == Player.STATE_ENDED) {
                            playerConnection.player.seekTo(0, 0)
                            playerConnection.player.playWhenReady = true
                        } else {
                            playerConnection.togglePlayPause()
                        }
                    },
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(28.dp),
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(enabled = canSkipNext && !isListenTogetherGuest) {
                        playerConnection.player.seekToNext()
                    },
        ) {
            Icon(
                painter = painterResource(R.drawable.skip_next),
                contentDescription = null,
                tint = if (canSkipNext) tint else tint.copy(alpha = 0.4f),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
