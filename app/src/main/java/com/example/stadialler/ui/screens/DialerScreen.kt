package com.example.stadialler.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.stadialler.model.Contact
import com.example.stadialler.ui.components.DialPad
import com.example.stadialler.ui.theme.SamsungBlue
import com.example.stadialler.ui.theme.SamsungDarkBg
import com.example.stadialler.ui.theme.SamsungDivider
import com.example.stadialler.ui.theme.SamsungGreen
import com.example.stadialler.ui.theme.SamsungSurface
import com.example.stadialler.ui.theme.SamsungSurfaceVariant
import com.example.stadialler.ui.theme.SamsungTextMuted
import com.example.stadialler.ui.theme.SamsungTextPrimary
import com.example.stadialler.ui.theme.SamsungTextSecondary
import com.example.stadialler.viewmodel.DialerViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialerScreen(
    viewModel: DialerViewModel,
    onNavigateToUpdates: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToContacts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dialInput by viewModel.dialInput.collectAsState()
    val suggestions by viewModel.matchedSuggestions.collectAsState()
    val listState = rememberLazyListState()

    var showMenu by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    var clipboardNumber by remember { mutableStateOf<String?>(null) }

    fun checkClipboard() {
        try {
            val clipText = clipboardManager.getText()?.text?.trim()
            if (!clipText.isNullOrBlank()) {
                val clean = clipText.replace(Regex("[^0-9+*#]"), "")
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

    LaunchedEffect(dialInput) {
        checkClipboard()
    }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SamsungDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Samsung One UI Top App Bar: [Search] [3-Dots Menu]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateToContacts,
                    modifier = Modifier.size(42.dp).testTag("samsung_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث / Search",
                        tint = SamsungTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(42.dp).testTag("samsung_more_options")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "خيارات إضافية / More Options",
                            tint = SamsungTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Samsung One UI Dropdown Menu
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(SamsungSurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("أرقام الاتصال السريع", color = SamsungTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Speed, null, tint = SamsungGreen, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showMenu = false
                                onNavigateToContacts()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("الضبط / Settings", color = SamsungTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, null, tint = SamsungBlue, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showMenu = false
                                onNavigateToSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("تحديثات التطبيق", color = SamsungTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.SystemUpdate, null, tint = SamsungBlue, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showMenu = false
                                onNavigateToUpdates()
                            }
                        )
                    }
                }
            }

            // 2. Smart Paste Pill (When clipboard has copied number)
            AnimatedVisibility(
                visible = clipboardNumber != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SamsungSurfaceVariant)
                            .border(1.dp, SamsungDivider, RoundedCornerShape(20.dp))
                            .clickable {
                                clipboardNumber?.let {
                                    val clean = it.replace(Regex("[^0-9+*#,;]"), "")
                                    viewModel.setDialInput(clean)
                                    clipboardNumber = null
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                            .testTag("paste_clipboard_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = null,
                            tint = SamsungGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "لصق الرقم المنسوخ: ${clipboardNumber?.take(16)}",
                            color = SamsungTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 3. Samsung Viewing Area (Suggestions appear only when typing)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (dialInput.isNotEmpty()) {
                    if (suggestions.isEmpty()) {
                        // Clean empty state when no matching contacts
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد أسماء مطابقة",
                                color = SamsungTextMuted,
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
                            items(suggestions, key = { it.id }) { contact ->
                                SamsungSuggestionItem(
                                    contact = contact,
                                    onCallClick = {
                                        viewModel.startCall(contact.phoneNumber, contact.name)
                                    },
                                    onItemClick = {
                                        viewModel.setDialInput(contact.phoneNumber)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Samsung One UI "+ Add to contacts" Action Pill (appears when dialing)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                contentAlignment = Alignment.Center
            ) {
                if (dialInput.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(SamsungSurfaceVariant)
                            .clickable {
                                onNavigateToContacts()
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("add_to_contacts_pill"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = SamsungGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "إضافة إلى جهات الاتصال",
                            color = SamsungTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 5. Dialed Number Field (Centered, completely borderless & transparent)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { showContextMenu = true }
                    )
                    .padding(horizontal = 8.dp)
                    .testTag("dial_input_box"),
                contentAlignment = Alignment.Center
            ) {
                if (dialInput.isNotEmpty()) {
                    Text(
                        text = dialInput,
                        color = SamsungTextPrimary,
                        fontSize = if (dialInput.length > 13) 26.sp else 34.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dial_input_text")
                    )

                    // Quick Copy Icon on the left side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
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
                                contentDescription = "نسخ الرقم",
                                tint = SamsungTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Long-press Context Menu for Copy / Paste
                DropdownMenu(
                    expanded = showContextMenu,
                    onDismissRequest = { showContextMenu = false },
                    modifier = Modifier.background(SamsungSurfaceVariant)
                ) {
                    if (dialInput.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { Text("نسخ الرقم", color = SamsungTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, null, tint = SamsungGreen, modifier = Modifier.size(18.dp))
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
                            text = { Text("لصق", color = SamsungTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.ContentPaste, null, tint = SamsungGreen, modifier = Modifier.size(18.dp))
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

            // 6. Samsung Keypad & Green Call Button
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
}

@Composable
fun SamsungSuggestionItem(
    contact: Contact,
    onCallClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SamsungSurface)
            .clickable(onClick = onItemClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("suggestion_item_${contact.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SamsungSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1).uppercase(),
                    color = SamsungGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = contact.name,
                    color = SamsungTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = contact.phoneNumber,
                    color = SamsungTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        IconButton(
            onClick = onCallClick,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SamsungGreen.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "اتصال",
                tint = SamsungGreen,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}
