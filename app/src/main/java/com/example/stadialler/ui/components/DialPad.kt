package com.example.stadialler.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stadialler.ui.theme.SamsungGreen
import com.example.stadialler.ui.theme.SamsungSurfaceVariant
import com.example.stadialler.ui.theme.SamsungTextMuted
import com.example.stadialler.ui.theme.SamsungTextPrimary
import com.example.stadialler.ui.theme.SamsungTextSecondary

data class KeypadKey(
    val digit: Char,
    val enLetters: String,
    val arLetters: String,
    val speedDialSlot: Int? = null
)

val samsungKeypadRows = listOf(
    listOf(
        KeypadKey('1', "", "", 1),
        KeypadKey('2', "ABC", "أ ب ت ث", 2),
        KeypadKey('3', "DEF", "ج ح خ د", 3)
    ),
    listOf(
        KeypadKey('4', "GHI", "ذ ر ز س", 4),
        KeypadKey('5', "JKL", "ش ص ض ط", 5),
        KeypadKey('6', "MNO", "ظ ع غ ف", 6)
    ),
    listOf(
        KeypadKey('7', "PQRS", "ق ك ل م", 7),
        KeypadKey('8', "TUV", "ن ه ة و ؤ", 8),
        KeypadKey('9', "WXYZ", "ي ى ئ ء", 9)
    ),
    listOf(
        KeypadKey('*', "", "", null),
        KeypadKey('0', "+", "+", null),
        KeypadKey('#', "", "", null)
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialPad(
    onDigitPress: (Char) -> Unit,
    onSpeedDialLongPress: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onStartCall: (isCarrier: Boolean) -> Unit,
    hasInput: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        samsungKeypadRows.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowKeys.forEach { key ->
                    SamsungKeypadButton(
                        key = key,
                        onClick = { onDigitPress(key.digit) },
                        onLongClick = {
                            if (key.digit == '0') {
                                onDigitPress('+')
                            } else if (key.speedDialSlot != null) {
                                onSpeedDialLongPress(key.digit)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Samsung Bottom Call Row: [Video Call] [Signature Samsung Green Pill Call Button] [Backspace]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Action: Samsung Video Call Button (or placeholder)
            Box(
                modifier = Modifier.size(62.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasInput) {
                    IconButton(
                        onClick = { onStartCall(false) },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(SamsungSurfaceVariant)
                            .testTag("video_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = SamsungTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Center Action: Signature Samsung Green Call Pill Button
            Box(
                modifier = Modifier
                    .width(84.dp)
                    .height(58.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(SamsungGreen)
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White),
                        onClick = { onStartCall(false) },
                        onLongClick = { onStartCall(true) }
                    )
                    .testTag("call_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Right Action: Samsung Backspace Button
            Box(
                modifier = Modifier.size(62.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasInput) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White),
                                onClick = onBackspace,
                                onLongClick = onClearAll
                            )
                            .testTag("backspace_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = SamsungTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SamsungKeypadButton(
    key: KeypadKey,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(100.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = SamsungGreen.copy(alpha = 0.25f)),
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("keypad_${key.digit}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = key.digit.toString(),
                color = SamsungTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 28.sp
            )

            if (key.digit == '1') {
                Icon(
                    imageVector = Icons.Default.Voicemail,
                    contentDescription = "Voicemail",
                    tint = SamsungTextMuted,
                    modifier = Modifier.size(13.dp)
                )
            } else if (key.digit in '2'..'9') {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = key.enLetters,
                        color = SamsungTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "• ${key.arLetters}",
                        color = SamsungTextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            } else if (key.digit == '0') {
                Text(
                    text = "+",
                    color = SamsungTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Spacer(modifier = Modifier.height(11.dp))
            }
        }
    }
}
