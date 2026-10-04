package com.example.stadialler.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.stadialler.model.Contact
import com.example.stadialler.ui.components.DialPad
import com.example.stadialler.ui.theme.AmberUpdate
import com.example.stadialler.ui.theme.CyanAccent
import com.example.stadialler.ui.theme.CyanLight
import com.example.stadialler.ui.theme.CyanPrimary
import com.example.stadialler.ui.theme.DarkBackground
import com.example.stadialler.ui.theme.DarkSurface
import com.example.stadialler.ui.theme.DarkSurfaceVariant
import com.example.stadialler.ui.theme.GreenConnect
import com.example.stadialler.ui.theme.KeypadButtonBorder
import com.example.stadialler.ui.theme.TextMuted
import com.example.stadialler.ui.theme.TextPrimary
import com.example.stadialler.ui.theme.TextSecondary
import com.example.stadialler.viewmodel.DialerViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialerScreen(
    viewModel: DialerViewModel,
    onNavigateToUpdates: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dialInput by viewModel.dialInput.collectAsState()
    val suggestions by viewModel.matchedSuggestions.collectAsState()
    val sipExt by viewModel.sipExt.collectAsState()
    val isTls by viewModel.isTls.collectAsState()
    val availableUpdate by viewModel.availableUpdate.collectAsState()

    var isKeypadVisible by remember { mutableStateOf(true) }
    var showContextMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val clipboardManager = LocalClipboardManager.current
    var clipboardNumber by remember { mutableStateOf<String?>(null) }

    // Helper to refresh clipboard number detection
    fun checkClipboard() {
        try {
            val clipText = clipboardManager.getText()?.text?.trim()
            if (!clipText.isNullOrBlank()) {
                val clean = clipText.replace(Regex("[^0-9+*#]"), "")
                // If it contains at least 3 digits and is not identical to current input
                if (clean.length >= 3 && clean != dialInput) {
                    clipboardNumber = clipText
                } else {
                    clipboardNumber = null
                }
            } else {
                clipboardNumber = null
            }
        } catch (e: Exception) {
            clipboardNumber = null
        }
    }

    // Refresh on composition and whenever dialInput changes
    LaunchedEffect(dialInput) {
        checkClipboard()
    }

    // Refresh whenever activity is resumed (user copied number from another app and switched back)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkClipboard()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Hide keypad when scrolling up in suggestions list
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if ((index > 0 || offset > 30) && isKeypadVisible) {
                    isKeypadVisible = false
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Bar: PBX Status, Keypad Toggle & Update Pill
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // PBX Status Chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = "Online Status",
                            tint = GreenConnect,
                            modifier = Modifier.size(8.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EXT $sipExt • ${if (isTls) "TLS 1.3" else "UDP"}",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Keypad Toggle / Hide indicator when visible
                    if (isKeypadVisible && suggestions.isNotEmpty()) {
                        IconButton(
                            onClick = { isKeypadVisible = false },
                            modifier = Modifier.size(32.dp).testTag("collapse_keypad_arrow")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Hide Keypad",
                                tint = TextSecondary
                            )
                        }
                    }

                    // Update notification badge if update available from repo feed
                    if (availableUpdate != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(AmberUpdate.copy(alpha = 0.2f))
                                .border(1.dp, AmberUpdate, RoundedCornerShape(20.dp))
                                .clickable { onNavigateToUpdates() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("dialer_update_pill")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "Update Available",
                                tint = AmberUpdate,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "v${availableUpdate?.versionName}",
                                color = AmberUpdate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 2. Requirement: "في حالة تم نسخ رقم من مكان اخر يظهر في الاعلى زر Paste number from clipboard"
                AnimatedVisibility(
                    visible = clipboardNumber != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyanPrimary.copy(alpha = 0.16f))
                            .border(1.dp, CyanPrimary, RoundedCornerShape(12.dp))
                            .clickable {
                                clipboardNumber?.let {
                                    val clean = it.replace(Regex("[^0-9+*#,;]"), "")
                                    viewModel.setDialInput(clean)
                                    clipboardNumber = null
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("paste_clipboard_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Paste number from clipboard",
                                    color = CyanLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = clipboardNumber ?: "",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Paste",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 3. Suggestions List (Above the dialed number box!)
            // Requirement: Hidden completely when empty; only shows matching suggestions when typing!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 2.dp)
            ) {
                if (dialInput.isNotEmpty()) {
                    if (suggestions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد أسماء مطابقة للأرقام المدخلة",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("suggestions_list"),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                Text(
                                    text = "الاقتراحات المطابقة (${suggestions.size})",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            items(suggestions, key = { it.id }) { contact ->
                                SuggestionContactCard(
                                    contact = contact,
                                    onCallClick = {
                                        viewModel.startCall(contact.phoneNumber, contact.name)
                                    },
                                    onCardClick = {
                                        viewModel.setDialInput(contact.phoneNumber)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Requirement: "حقل الكتابة يكون بدون خلفية او حواف و بدون اي شئ داخله"
            // Requirement: "اجعل الكتابة تكون من المنتصف في حقل الكتابة بدلا من جهة اليسار"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .combinedClickable(
                        onClick = { isKeypadVisible = true },
                        onLongClick = { showContextMenu = true }
                    )
                    .padding(horizontal = 8.dp)
                    .testTag("dial_input_box"),
                contentAlignment = Alignment.Center
            ) {
                // Centered Dialed Number (nothing rendered inside when empty!)
                if (dialInput.isNotEmpty()) {
                    Text(
                        text = dialInput,
                        color = TextPrimary,
                        fontSize = if (dialInput.length > 13) 24.sp else 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dial_input_text")
                    )

                    // Action Controls on Left and Right sides when dialInput is not empty
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(dialInput))
                            },
                            modifier = Modifier.size(38.dp).testTag("copy_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy number",
                                tint = TextSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.onBackspace() },
                            modifier = Modifier.size(38.dp).testTag("inline_backspace_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Delete",
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Long-press Context Menu for Copy / Paste
                DropdownMenu(
                    expanded = showContextMenu,
                    onDismissRequest = { showContextMenu = false },
                    modifier = Modifier.background(DarkSurfaceVariant)
                ) {
                    if (dialInput.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { Text("نسخ الرقم / Copy", color = TextPrimary) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = CyanAccent)
                            },
                            onClick = {
                                clipboardManager.setText(AnnotatedString(dialInput))
                                showContextMenu = false
                            }
                        )
                    }
                    val clip = clipboardManager.getText()?.text
                    if (!clip.isNullOrBlank()) {
                        DropdownMenuItem(
                            text = { Text("لصق / Paste", color = TextPrimary) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, tint = CyanAccent)
                            },
                            onClick = {
                                val clean = clip.replace(Regex("[^0-9+*#,;]"), "")
                                viewModel.setDialInput(clean)
                                showContextMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 5. Dialpad Keypad (With increased button height to 64dp!)
            AnimatedVisibility(
                visible = isKeypadVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                DialPad(
                    onDigitPress = { digit -> viewModel.onKeyPress(digit) },
                    onSpeedDialLongPress = { digit -> viewModel.onSpeedDialLongPress(digit) },
                    onBackspace = { viewModel.onBackspace() },
                    onClearAll = { viewModel.onClearDialInput() },
                    onStartCall = { isCarrier ->
                        viewModel.startCall(dialInput, launchSystemDialer = isCarrier)
                    },
                    hasInput = dialInput.isNotEmpty()
                )
            }
        }

        // 6. Floating Action Button at Bottom-Right when keypad is hidden
        AnimatedVisibility(
            visible = !isKeypadVisible,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
        ) {
            FloatingActionButton(
                onClick = { isKeypadVisible = true },
                containerColor = CyanPrimary,
                contentColor = DarkBackground,
                modifier = Modifier.testTag("show_keypad_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Dialpad,
                    contentDescription = "إظهار لوحة الأرقام / Show Keypad",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun SuggestionContactCard(
    contact: Contact,
    onCallClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, KeypadButtonBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onCardClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("suggestion_card_${contact.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(1.dp, CyanPrimary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1).uppercase(),
                    color = CyanAccent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (contact.speedDialKey != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyanPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "سرعة ${contact.speedDialKey}",
                                color = CyanAccent,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "${contact.phoneNumber}${if (!contact.extension.isNullOrBlank()) " • تحويلة ${contact.extension}" else ""}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        IconButton(
            onClick = onCallClick,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(GreenConnect.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "اتصال",
                tint = GreenConnect,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
