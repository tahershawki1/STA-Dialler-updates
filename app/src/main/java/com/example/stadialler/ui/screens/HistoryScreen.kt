package com.example.stadialler.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stadialler.model.CallRecord
import com.example.stadialler.model.CallType
import com.example.stadialler.ui.theme.SamsungBlue
import com.example.stadialler.ui.theme.SamsungDarkBg
import com.example.stadialler.ui.theme.SamsungGreen
import com.example.stadialler.ui.theme.SamsungRed
import com.example.stadialler.ui.theme.SamsungSurface
import com.example.stadialler.ui.theme.SamsungSurfaceVariant
import com.example.stadialler.ui.theme.SamsungTextMuted
import com.example.stadialler.ui.theme.SamsungTextPrimary
import com.example.stadialler.ui.theme.SamsungTextSecondary
import com.example.stadialler.viewmodel.DialerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: DialerViewModel,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val history by viewModel.filteredHistory.collectAsState()
    val currentFilter by viewModel.historyFilter.collectAsState()
    val historySearchQuery by viewModel.historySearchQuery.collectAsState()
    val context = LocalContext.current

    var isSearchActive by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    // Grouping calls chronologically by date
    val groupedHistory = remember(history) {
        val sorted = history.sortedByDescending { it.timestamp }
        sorted.groupBy { record -> getDateHeader(record.timestamp) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SamsungDarkBg)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("history_screen")
    ) {
        // 1. Samsung One UI Large Header & Top Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الأخيرة",
                color = SamsungTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { isSearchActive = !isSearchActive },
                    modifier = Modifier.size(40.dp).testTag("recents_search_btn")
                ) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = SamsungTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(40.dp).testTag("recents_menu_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "خيارات",
                            tint = SamsungTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(SamsungSurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("حذف سجل المكالمات", color = SamsungRed) },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteSweep, null, tint = SamsungRed, modifier = Modifier.size(20.dp))
                            },
                            onClick = {
                                showMenu = false
                                showClearDialog = true
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
                    }
                }
            }
        }

        // Real-time Search Input Field in Recents
        if (isSearchActive) {
            OutlinedTextField(
                value = historySearchQuery,
                onValueChange = { viewModel.setHistorySearchQuery(it) },
                placeholder = { Text("بحث في سجل المكالمات بالاسم أو الرقم...", color = SamsungTextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SamsungTextPrimary,
                    unfocusedTextColor = SamsungTextPrimary,
                    focusedBorderColor = SamsungGreen,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = SamsungSurfaceVariant,
                    unfocusedContainerColor = SamsungSurfaceVariant
                ),
                trailingIcon = {
                    if (historySearchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setHistorySearchQuery("") }) {
                            Icon(Icons.Default.Close, "مسح", tint = SamsungTextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("recents_search_field")
            )
        }

        // 2. Samsung One UI Filter Tabs: [الكل] [الفائتة]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentFilter == null,
                onClick = { viewModel.setHistoryFilter(null) },
                label = { Text("الكل", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SamsungGreen,
                    selectedLabelColor = Color.White,
                    containerColor = SamsungSurfaceVariant,
                    labelColor = SamsungTextSecondary
                )
            )
            FilterChip(
                selected = currentFilter == CallType.MISSED,
                onClick = { viewModel.setHistoryFilter(CallType.MISSED) },
                label = { Text("المكالمات الفائتة", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SamsungRed,
                    selectedLabelColor = Color.White,
                    containerColor = SamsungSurfaceVariant,
                    labelColor = SamsungTextSecondary
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = SamsungTextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "لا توجد مكالمات حديثة",
                        color = SamsungTextSecondary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ستظهر المكالمات الصادرة والواردة والفائتة هنا",
                        color = SamsungTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                groupedHistory.forEach { (dateHeader, records) ->
                    item(key = "header_$dateHeader") {
                        Text(
                            text = dateHeader,
                            color = SamsungTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 6.dp, top = 12.dp, bottom = 4.dp)
                        )
                    }

                    items(records, key = { it.id }) { record ->
                        SamsungRecentsItem(
                            record = record,
                            onCallClick = {
                                viewModel.makeRealCall(context, record.number, record.contactName)
                            },
                            onMessageClick = {
                                viewModel.sendSms(context, record.number)
                            },
                            onVideoClick = {
                                viewModel.startVideoCall(context, record.number)
                            },
                            onDeleteClick = {
                                viewModel.deleteCallRecord(record.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("مسح سجل المكالمات؟", color = SamsungTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("سيتم مسح جميع المكالمات من السجل نهائياً.", color = SamsungTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCallHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("مسح الكل", color = SamsungRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("إلغاء", color = SamsungTextSecondary)
                }
            },
            containerColor = SamsungSurfaceVariant,
            shape = RoundedCornerShape(22.dp)
        )
    }
}

@Composable
fun SamsungRecentsItem(
    record: CallRecord,
    onCallClick: () -> Unit,
    onMessageClick: () -> Unit,
    onVideoClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val (directionIcon, directionColor) = when (record.type) {
        CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade to SamsungBlue
        CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived to SamsungGreen
        CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed to SamsungRed
        CallType.BLOCKED -> Icons.Default.Block to SamsungTextMuted
    }

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(record.timestamp) { timeFormat.format(Date(record.timestamp)) }

    val fullDateFormat = remember { SimpleDateFormat("yyyy/MM/dd • HH:mm:ss", Locale.getDefault()) }
    val fullDateTime = remember(record.timestamp) { fullDateFormat.format(Date(record.timestamp)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SamsungSurface)
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("recents_item_${record.id}")
    ) {
        // Main Row: [Avatar] [Name + Direction Arrow] [Time]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Samsung Contact Circle Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SamsungSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (record.contactName ?: record.number).take(1).uppercase(),
                        color = if (record.type == CallType.MISSED) SamsungRed else SamsungTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = record.contactName ?: record.number,
                        color = if (record.type == CallType.MISSED) SamsungRed else SamsungTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = directionIcon,
                            contentDescription = record.type.name,
                            tint = directionColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = record.carrierOrLine,
                            color = SamsungTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Call Time on the right
            Text(
                text = formattedTime,
                color = SamsungTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // Samsung Expandable 4-Button Quick Action Bar: [Call] [Message] [Video] [Details]
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
            ) {
                // Detail Info Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = record.number,
                        color = SamsungTextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = fullDateTime,
                        color = SamsungTextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Samsung Iconic 4 Action Circles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SamsungSurfaceVariant)
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Call (Green)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onCallClick)
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SamsungGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "اتصال",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("اتصال", color = SamsungTextPrimary, fontSize = 11.sp)
                    }

                    // 2. Message (Blue)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onMessageClick)
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SamsungBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "رسالة",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("رسالة", color = SamsungTextPrimary, fontSize = 11.sp)
                    }

                    // 3. Video Call
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onVideoClick)
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SamsungSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "مكالمة فيديو",
                                tint = SamsungGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("فيديو", color = SamsungTextPrimary, fontSize = 11.sp)
                    }

                    // 4. Details / Delete
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onDeleteClick)
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SamsungSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "حذف السجل",
                                tint = SamsungTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("حذف", color = SamsungRed, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

private fun getDateHeader(timestamp: Long): String {
    val calRecord = Calendar.getInstance().apply { timeInMillis = timestamp }
    val calNow = Calendar.getInstance()

    val isToday = calRecord.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
            calRecord.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)

    if (isToday) return "اليوم"

    val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = calRecord.get(Calendar.YEAR) == calYesterday.get(Calendar.YEAR) &&
            calRecord.get(Calendar.DAY_OF_YEAR) == calYesterday.get(Calendar.DAY_OF_YEAR)

    if (isYesterday) return "أمس"

    val format = SimpleDateFormat("EEEE، d MMMM", Locale("ar"))
    return try {
        format.format(Date(timestamp))
    } catch (e: Exception) {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
