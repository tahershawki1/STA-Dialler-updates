package com.example.stadialler.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stadialler.model.CallRecord
import com.example.stadialler.model.CallType
import com.example.stadialler.ui.theme.CyanAccent
import com.example.stadialler.ui.theme.CyanLight
import com.example.stadialler.ui.theme.CyanPrimary
import com.example.stadialler.ui.theme.DarkBackground
import com.example.stadialler.ui.theme.DarkSurface
import com.example.stadialler.ui.theme.DarkSurfaceElevated
import com.example.stadialler.ui.theme.DarkSurfaceVariant
import com.example.stadialler.ui.theme.GreenConnect
import com.example.stadialler.ui.theme.KeypadButtonBorder
import com.example.stadialler.ui.theme.RedDisconnect
import com.example.stadialler.ui.theme.TextMuted
import com.example.stadialler.ui.theme.TextPrimary
import com.example.stadialler.ui.theme.TextSecondary
import com.example.stadialler.viewmodel.DialerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: DialerViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.filteredHistory.collectAsState()
    val currentFilter by viewModel.historyFilter.collectAsState()

    // Requirement 4: Sort calls with today's calls at top, grouped by date
    val groupedHistory = remember(history) {
        val sorted = history.sortedByDescending { it.timestamp }
        sorted.groupBy { record -> getDateHeader(record.timestamp) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("history_screen")
    ) {
        // Title Bar (Requirement 5: Clear All removed and moved to Settings)
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Text(
                text = "سجل المكالمات • CALL LOG",
                color = TextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "سجل الاتصالات والتحويلات المشفرة",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = currentFilter == null,
                onClick = { viewModel.setHistoryFilter(null) },
                label = { Text("الكل", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanPrimary,
                    selectedLabelColor = DarkBackground,
                    containerColor = DarkSurfaceVariant,
                    labelColor = TextSecondary
                )
            )
            FilterChip(
                selected = currentFilter == CallType.MISSED,
                onClick = { viewModel.setHistoryFilter(CallType.MISSED) },
                label = { Text("الفائتة", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RedDisconnect,
                    selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                    containerColor = DarkSurfaceVariant,
                    labelColor = TextSecondary
                )
            )
            FilterChip(
                selected = currentFilter == CallType.OUTGOING,
                onClick = { viewModel.setHistoryFilter(CallType.OUTGOING) },
                label = { Text("الصادرة", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanAccent,
                    selectedLabelColor = DarkBackground,
                    containerColor = DarkSurfaceVariant,
                    labelColor = TextSecondary
                )
            )
            FilterChip(
                selected = currentFilter == CallType.INCOMING,
                onClick = { viewModel.setHistoryFilter(CallType.INCOMING) },
                label = { Text("الواردة", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenConnect,
                    selectedLabelColor = DarkBackground,
                    containerColor = DarkSurfaceVariant,
                    labelColor = TextSecondary
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

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
                        tint = TextMuted,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد مكالمات مسجلة",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ستظهر المكالمات الصادرة والواردة والفائتة هنا",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Requirement 4: Grouped by date with section headers, today's calls at top
                groupedHistory.forEach { (dateHeader, records) ->
                    item(key = "header_$dateHeader") {
                        DateSectionHeader(title = dateHeader)
                    }

                    items(records, key = { it.id }) { record ->
                        CallHistoryItem(
                            record = record,
                            onCallClick = {
                                viewModel.startCall(record.number, record.contactName)
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
}

@Composable
fun DateSectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = CyanAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(DarkSurfaceVariant)
        )
    }
}

@Composable
fun CallHistoryItem(
    record: CallRecord,
    onCallClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    val (icon, tint) = when (record.type) {
        CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade to CyanAccent
        CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived to GreenConnect
        CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed to RedDisconnect
        CallType.BLOCKED -> Icons.Default.Block to TextMuted
    }

    // Requirement 3: Show ONLY call time in main row (e.g. 14:35 or 02:35 PM)
    val timeOnlyFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val callTimeOnly = remember(record.timestamp) { timeOnlyFormat.format(Date(record.timestamp)) }

    // Full date format for the hidden/expanded details section
    val fullDateFormat = remember { SimpleDateFormat("yyyy/MM/dd • HH:mm:ss", Locale.getDefault()) }
    val fullDateTime = remember(record.timestamp) { fullDateFormat.format(Date(record.timestamp)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, KeypadButtonBorder, RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("history_item_${record.id}")
    ) {
        // Main Single Row: [Direction Icon] [Contact Name] [Time Only] [Chevron]
        // Requirements 1 & 2: Delete icon and Call icon removed from main row!
        // Requirement 3: Number and full date removed from main row, showing time only!
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = record.type.name,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = record.contactName ?: record.number,
                    color = if (record.type == CallType.MISSED) RedDisconnect else TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            // Right side: Call Time Only + Expand Chevron
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = callTimeOnly,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Requirement 3: Expandable Hidden Section with full details on click!
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.6f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Phone Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "رقم الهاتف / Number:", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = record.number,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Full Date & Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "التاريخ والوقت / Timestamp:", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = fullDateTime,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Call Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "مدة المكالمة / Duration:", color = TextMuted, fontSize = 11.sp)
                    val durationText = if (record.durationSeconds > 0) {
                        "${record.durationSeconds / 60} دقيقة و ${record.durationSeconds % 60} ثانية (${record.durationSeconds}s)"
                    } else if (record.type == CallType.MISSED) {
                        "مكالمة فائتة (لم يُرد عليها)"
                    } else {
                        "0 ثانية"
                    }
                    Text(
                        text = durationText,
                        color = CyanLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Line / Carrier Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "الخط المستخدم / Trunk Line:", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = record.carrierOrLine,
                        color = GreenConnect,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!record.notes.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "الملاحظات / Notes:", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = record.notes,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons inside expanded view
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Call Button
                    Button(
                        onClick = onCallClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenConnect,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اتصال", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Copy Number Button
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(record.number))
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نسخ", color = CyanAccent, fontSize = 12.sp)
                    }

                    // Delete Record Button
                    OutlinedButton(
                        onClick = onDeleteClick,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف السجل",
                            tint = RedDisconnect,
                            modifier = Modifier.size(16.dp)
                        )
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

    if (isToday) return "اليوم • Today"

    val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = calRecord.get(Calendar.YEAR) == calYesterday.get(Calendar.YEAR) &&
            calRecord.get(Calendar.DAY_OF_YEAR) == calYesterday.get(Calendar.DAY_OF_YEAR)

    if (isYesterday) return "أمس • Yesterday"

    val format = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))
    return try {
        format.format(Date(timestamp))
    } catch (e: Exception) {
        SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
