package com.example.stadialler.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stadialler.ui.theme.CyanAccent
import com.example.stadialler.ui.theme.CyanLight
import com.example.stadialler.ui.theme.CyanPrimary
import com.example.stadialler.ui.theme.DarkBackground
import com.example.stadialler.ui.theme.DarkSurface
import com.example.stadialler.ui.theme.DarkSurfaceVariant
import com.example.stadialler.ui.theme.GreenConnect
import com.example.stadialler.ui.theme.KeypadButtonBg
import com.example.stadialler.ui.theme.KeypadButtonBorder
import com.example.stadialler.ui.theme.RedDisconnect
import com.example.stadialler.ui.theme.TextMuted
import com.example.stadialler.ui.theme.TextPrimary
import com.example.stadialler.ui.theme.TextSecondary
import com.example.stadialler.viewmodel.CallState

@Composable
fun ActiveCallSheet(
    callState: CallState,
    number: String,
    contactName: String?,
    durationSeconds: Long,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isOnHold: Boolean,
    isRecording: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleHold: () -> Unit,
    onToggleRecord: () -> Unit,
    onSendDtmf: (Char) -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showInCallDtmf by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .testTag("active_call_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Security & Telephony status banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted Line",
                        tint = GreenConnect,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "STA SECURE VOIP • TLS 1.3 / SRTP",
                        color = GreenConnect,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Avatar / Calling target
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .border(2.dp, if (callState == CallState.CONNECTED) GreenConnect else CyanPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (contactName ?: number).take(1).uppercase(),
                        color = CyanAccent,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = contactName ?: "Unknown Contact",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = number,
                    color = TextSecondary,
                    fontSize = 17.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status & Duration Timer
                val statusText = when (callState) {
                    CallState.DIALING -> "Dialing PBX extension..."
                    CallState.RINGING -> "Ringing terminal..."
                    CallState.CONNECTED -> formatDuration(durationSeconds)
                    CallState.ON_HOLD -> "Call on Hold (${formatDuration(durationSeconds)})"
                    CallState.ENDED -> "Call Terminated"
                    CallState.IDLE -> ""
                }

                val statusColor = when (callState) {
                    CallState.CONNECTED -> GreenConnect
                    CallState.ON_HOLD -> CyanLight
                    CallState.ENDED -> RedDisconnect
                    else -> CyanAccent
                }

                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                // Recording Indicator if active
                if (isRecording) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = "Recording",
                            tint = RedDisconnect,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "SECURE LOCAL RECORDING ACTIVE",
                            color = RedDisconnect,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Audio Waveform Visualization
            if (callState == CallState.CONNECTED) {
                AudioWaveformVisualizer(
                    isMuted = isMuted,
                    isOnHold = isOnHold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 32.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }

            // In-Call DTMF Drawer or Controls Grid
            if (showInCallDtmf) {
                InCallDtmfGrid(
                    onDigitPress = onSendDtmf,
                    onClose = { showInCallDtmf = false }
                )
            } else {
                // 2x3 Grid of In-Call Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InCallActionButton(
                            icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            label = if (isMuted) "Unmute" else "Mute",
                            isActive = isMuted,
                            onClick = onToggleMute,
                            testTag = "incall_mute"
                        )
                        InCallActionButton(
                            icon = Icons.Default.Dialpad,
                            label = "Keypad",
                            isActive = false,
                            onClick = { showInCallDtmf = true },
                            testTag = "incall_keypad"
                        )
                        InCallActionButton(
                            icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            label = if (isSpeakerOn) "Speaker" else "Earpiece",
                            isActive = isSpeakerOn,
                            onClick = onToggleSpeaker,
                            testTag = "incall_speaker"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InCallActionButton(
                            icon = if (isOnHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                            label = if (isOnHold) "Resume" else "Hold",
                            isActive = isOnHold,
                            onClick = onToggleHold,
                            testTag = "incall_hold"
                        )
                        InCallActionButton(
                            icon = Icons.Default.FiberManualRecord,
                            label = if (isRecording) "Stop Rec" else "Record",
                            isActive = isRecording,
                            activeColor = RedDisconnect,
                            onClick = onToggleRecord,
                            testTag = "incall_record"
                        )
                        InCallActionButton(
                            icon = Icons.Default.Security,
                            label = "Security",
                            isActive = true,
                            activeColor = GreenConnect,
                            onClick = {},
                            testTag = "incall_crypto"
                        )
                    }
                }
            }

            // Big Red End Call Button
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(RedDisconnect)
                    .clickable { onEndCall() }
                    .testTag("end_call_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun InCallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = CyanPrimary,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else DarkSurfaceVariant)
                .border(1.dp, if (isActive) activeColor else KeypadButtonBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) DarkBackground else TextPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (isActive) activeColor else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AudioWaveformVisualizer(
    isMuted: Boolean,
    isOnHold: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val barCount = 28
        val spacing = size.width / barCount
        val barWidth = spacing * 0.55f
        val centerY = size.height / 2f

        for (i in 0 until barCount) {
            val amplitude = if (isMuted || isOnHold) {
                4f
            } else {
                val wave1 = kotlin.math.sin(i * 0.4f + phase)
                val wave2 = kotlin.math.cos(i * 0.25f - phase)
                (kotlin.math.abs(wave1 + wave2) * 0.5f).coerceIn(0.15f, 1.0f) * (size.height * 0.42f)
            }

            val x = i * spacing + (spacing - barWidth) / 2f
            val top = centerY - amplitude
            val height = amplitude * 2f

            drawRoundRect(
                color = if (isMuted || isOnHold) TextMuted else CyanAccent,
                topLeft = Offset(x, top),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}

@Composable
private fun InCallDtmfGrid(
    onDigitPress: (Char) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, KeypadButtonBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "IN-CALL DTMF TRANSMITTER",
                color = CyanAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "HIDE",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onClose() }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val digits = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf('*', '0', '#')
        )

        digits.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { digit ->
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(KeypadButtonBg)
                            .border(1.dp, KeypadButtonBorder, CircleShape)
                            .clickable { onDigitPress(digit) }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit.toString(),
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
}
