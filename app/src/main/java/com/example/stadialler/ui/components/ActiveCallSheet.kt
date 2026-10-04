package com.example.stadialler.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stadialler.ui.theme.SamsungGreen
import com.example.stadialler.ui.theme.SamsungRed
import com.example.stadialler.ui.theme.SamsungTextMuted
import com.example.stadialler.ui.theme.SamsungTextPrimary
import com.example.stadialler.ui.theme.SamsungTextSecondary
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

    // Samsung In-Call Iconic Deep Gradient Background
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F1E36),
            Color(0xFF0A1324),
            Color(0xFF050912),
            Color(0xFF000000)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(horizontal = 24.dp, vertical = 36.dp)
            .testTag("active_call_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Caller Profile (Avatar, Name, Number, Duration)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                // Large Samsung Caller Circle Avatar
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E2C44)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (contactName ?: number).take(1).uppercase(),
                        color = SamsungGreen,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Caller Name
                Text(
                    text = contactName ?: number,
                    color = SamsungTextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                if (contactName != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = number,
                        color = SamsungTextSecondary,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // SIM Pill & Encryption
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF16233B))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "SIM 1 • STA SIP TLS",
                        color = SamsungTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Call State & Timer
                val statusText = when (callState) {
                    CallState.DIALING -> "جارِ الاتصال..."
                    CallState.RINGING -> "رنين..."
                    CallState.CONNECTED -> {
                        val m = durationSeconds / 60
                        val s = durationSeconds % 60
                        String.format("%02d:%02d", m, s)
                    }
                    CallState.ON_HOLD -> "قيد الانتظار"
                    CallState.ENDED -> "تم إنهاء المكالمة"
                    CallState.IDLE -> ""
                }
                Text(
                    text = statusText,
                    color = if (callState == CallState.CONNECTED) SamsungGreen else SamsungTextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 2. In-Call Keypad (If toggled)
            AnimatedVisibility(visible = showInCallDtmf) {
                InCallKeypadGrid(onDigitClick = onSendDtmf)
            }

            // 3. Samsung Iconic 6-Grid In-Call Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Record
                    SamsungInCallButton(
                        icon = Icons.Default.FiberManualRecord,
                        label = if (isRecording) "تسجيل..." else "تسجيل",
                        isActive = isRecording,
                        activeColor = SamsungRed,
                        onClick = onToggleRecord
                    )

                    // Hold
                    SamsungInCallButton(
                        icon = if (isOnHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                        label = if (isOnHold) "استئناف" else "إيقاف مؤقت",
                        isActive = isOnHold,
                        onClick = onToggleHold
                    )

                    // Bluetooth / Audio
                    SamsungInCallButton(
                        icon = Icons.Default.Bluetooth,
                        label = "بلوتوث",
                        isActive = false,
                        onClick = { /* Toggle Bluetooth */ }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Speaker
                    SamsungInCallButton(
                        icon = Icons.Default.VolumeUp,
                        label = "مكبر الصوت",
                        isActive = isSpeakerOn,
                        onClick = onToggleSpeaker
                    )

                    // Mute
                    SamsungInCallButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (isMuted) "تم الكتم" else "كتم الصوت",
                        isActive = isMuted,
                        onClick = onToggleMute
                    )

                    // Keypad
                    SamsungInCallButton(
                        icon = Icons.Default.Dialpad,
                        label = "لوحة المفاتيح",
                        isActive = showInCallDtmf,
                        onClick = { showInCallDtmf = !showInCallDtmf }
                    )
                }
            }

            // 4. Samsung Iconic Circular Red End Call Button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SamsungRed)
                    .clickable(onClick = onEndCall)
                    .testTag("end_call_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "إنهاء المكالمة",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
fun SamsungInCallButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = SamsungGreen,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else Color(0xFF1E283C)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else SamsungTextPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (isActive) activeColor else SamsungTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InCallKeypadGrid(onDigitClick: (Char) -> Unit) {
    val digits = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf('*', '0', '#')
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF121B2B))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        digits.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { d ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable { onDigitClick(d) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = d.toString(),
                            color = SamsungTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
