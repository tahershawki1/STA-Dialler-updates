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
import androidx.compose.material.icons.filled.PhoneInTalk
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
import com.example.stadialler.ui.theme.CyanAccent
import com.example.stadialler.ui.theme.CyanLight
import com.example.stadialler.ui.theme.CyanPrimary
import com.example.stadialler.ui.theme.DarkSurfaceVariant
import com.example.stadialler.ui.theme.GreenConnect
import com.example.stadialler.ui.theme.TextMuted
import com.example.stadialler.ui.theme.TextPrimary
import com.example.stadialler.ui.theme.TextSecondary

data class KeypadKey(
    val digit: Char,
    val enLetters: String,
    val arLetters: String,
    val speedDialSlot: Int? = null
)

private val keypadRows = listOf(
    listOf(
        KeypadKey('1', "VOICEMAIL", "بريد صوتي", 1),
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
        KeypadKey('8', "TUV", "ن هـ و", 8),
        KeypadKey('9', "WXYZ", "ي ى ء ة", 9)
    ),
    listOf(
        KeypadKey('*', "PAUSE", "*"),
        KeypadKey('0', "+", "+", null),
        KeypadKey('#', "WAIT", "#")
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
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        keypadRows.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowKeys.forEach { key ->
                    KeypadButton(
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

        // Bottom Action Bar: [Carrier Quick Call] [BIG CALL BUTTON] [BACKSPACE]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Carrier fallback call
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasInput) {
                    IconButton(
                        onClick = { onStartCall(true) },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .testTag("carrier_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = "Carrier Cellular Call",
                            tint = CyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Main Primary Call Button (Green)
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(GreenConnect)
                    .testTag("call_button")
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White),
                        onClick = { onStartCall(false) },
                        onLongClick = { onStartCall(true) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Initiate Secure Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Backspace Button
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasInput) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .testTag("backspace_button")
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = Color.White),
                                onClick = onBackspace,
                                onLongClick = onClearAll
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Delete Digit",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeypadButton(
    key: KeypadKey,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Increased button height for comfortable touch response
    Box(
        modifier = modifier
            .width(98.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = CyanPrimary.copy(alpha = 0.35f)),
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
                color = TextPrimary,
                fontSize = 27.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 27.sp
            )
            if (key.digit in '2'..'9') {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = key.enLetters,
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "• ${key.arLetters}",
                        color = CyanLight.copy(alpha = 0.9f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            } else if (key.enLetters.isNotEmpty()) {
                Text(
                    text = key.enLetters,
                    color = TextMuted,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
