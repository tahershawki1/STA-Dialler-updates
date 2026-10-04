package com.example.stadialler.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stadialler.ui.theme.CyanAccent
import com.example.stadialler.ui.theme.CyanPrimary
import com.example.stadialler.ui.theme.DarkBackground
import com.example.stadialler.ui.theme.DarkSurface
import com.example.stadialler.ui.theme.DarkSurfaceVariant
import com.example.stadialler.ui.theme.GreenConnect
import com.example.stadialler.ui.theme.KeypadButtonBorder
import com.example.stadialler.ui.theme.RedDisconnect
import com.example.stadialler.ui.theme.TextMuted
import com.example.stadialler.ui.theme.TextPrimary
import com.example.stadialler.ui.theme.TextSecondary
import com.example.stadialler.viewmodel.DialerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DialerViewModel,
    modifier: Modifier = Modifier
) {
    val isDtmfSound by viewModel.isDtmfSound.collectAsState()
    val isHaptic by viewModel.isHaptic.collectAsState()
    val isAutoRecord by viewModel.isAutoRecord.collectAsState()
    val sipServer by viewModel.sipServer.collectAsState()
    val sipPort by viewModel.sipPort.collectAsState()
    val sipExt by viewModel.sipExt.collectAsState()
    val isTls by viewModel.isTls.collectAsState()
    val selectedChannel by viewModel.selectedChannel.collectAsState()

    var feedUrl by remember { mutableStateOf(viewModel.repository.getUpdateFeedUrl()) }
    var channelDropdownExpanded by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    val channels = listOf("Enterprise / Tactical", "Stable", "Beta", "Canary")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "SETTINGS",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Telephony engine, SIP & update configuration",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Section: DTMF & Audio Preferences
        SettingsSection(
            title = "KEYPAD & AUDIO FEEDBACK",
            icon = Icons.Default.VolumeUp
        ) {
            SettingsSwitchRow(
                label = "DTMF Keypad Tones",
                sublabel = "Play dual-tone frequencies on dialer keystrokes",
                checked = isDtmfSound,
                onCheckedChange = { viewModel.updateDtmfSound(it) },
                testTag = "switch_dtmf_sound"
            )

            SettingsSwitchRow(
                label = "Haptic Vibration",
                sublabel = "Tactile impulse on keypad touch",
                checked = isHaptic,
                onCheckedChange = { viewModel.updateHaptic(it) },
                testTag = "switch_haptic"
            )

            SettingsSwitchRow(
                label = "Automatic Secure Recording",
                sublabel = "Initiate local call audio capture when connected",
                checked = isAutoRecord,
                onCheckedChange = { viewModel.updateAutoRecord(it) },
                testTag = "switch_auto_record"
            )
        }

        // Section: SIP & PBX Connection
        SettingsSection(
            title = "SIP / PBX TELEPHONY TRUNK",
            icon = Icons.Default.Router
        ) {
            OutlinedTextField(
                value = sipServer,
                onValueChange = { viewModel.updateSipServer(it) },
                label = { Text("SIP Domain / Server Host", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("field_sip_server"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = KeypadButtonBorder
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = sipPort,
                    onValueChange = { viewModel.updateSipPort(it) },
                    label = { Text("Port", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("field_sip_port"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = KeypadButtonBorder
                    )
                )

                OutlinedTextField(
                    value = sipExt,
                    onValueChange = { viewModel.updateSipExt(it) },
                    label = { Text("Extension / User", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("field_sip_ext"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = KeypadButtonBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingsSwitchRow(
                label = "Enforce TLS 1.3 & SRTP",
                sublabel = "Hardware-accelerated transport layer encryption",
                checked = isTls,
                onCheckedChange = { viewModel.updateTls(it) },
                testTag = "switch_tls"
            )
        }

        // Section: Update Feed Settings (STA-Dialler-updates)
        SettingsSection(
            title = "UPDATE REPO FEED CONFIGURATION",
            icon = Icons.Default.SystemUpdate
        ) {
            OutlinedTextField(
                value = feedUrl,
                onValueChange = {
                    feedUrl = it
                    viewModel.repository.setUpdateFeedUrl(it)
                },
                label = { Text("Signed APK Feed Endpoint", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("field_feed_url"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = KeypadButtonBorder
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            ExposedDropdownMenuBox(
                expanded = channelDropdownExpanded,
                onExpandedChange = { channelDropdownExpanded = !channelDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedChannel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Release Channel", color = TextSecondary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = channelDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor().testTag("channel_dropdown"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = KeypadButtonBorder
                    )
                )

                ExposedDropdownMenu(
                    expanded = channelDropdownExpanded,
                    onDismissRequest = { channelDropdownExpanded = false },
                    modifier = Modifier.background(DarkSurfaceVariant)
                ) {
                    channels.forEach { channel ->
                        DropdownMenuItem(
                            text = { Text(channel, color = TextPrimary) },
                            onClick = {
                                viewModel.updateChannel(channel)
                                channelDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Section: Call History & Data Management (Requirement 5)
        SettingsSection(
            title = "سجل المكالمات والبيانات • CALL HISTORY",
            icon = Icons.Default.History
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مسح سجل المكالمات بالكامل",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "حذف جميع سجلات المكالمات السابقة نهائياً من الذاكرة المحلية",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { showClearHistoryDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedDisconnect.copy(alpha = 0.2f),
                        contentColor = RedDisconnect
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("clear_history_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مسح الكل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section: System & Security Diagnostics
        SettingsSection(
            title = "CLIENT DIAGNOSTICS",
            icon = Icons.Default.Info
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DiagnosticRow(label = "Application", value = "STA-Dialler (Kotlin / Jetpack Compose)")
                DiagnosticRow(label = "Version", value = "v${viewModel.currentVersionName} (Build ${viewModel.currentVersionCode})")
                DiagnosticRow(label = "Application ID", value = "com.aistudio.stadialler.qvmrpx")
                DiagnosticRow(label = "Keystore Signing", value = "Debug / Release Signed (RSA-4096)")
                DiagnosticRow(label = "Target Platform", value = "Android 16 (API Level 36)")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("مسح سجل المكالمات بالكامل؟", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "هل أنت متأكد من رغبتك في حذف جميع سجلات المكالمات الصادرة والواردة والفائتة؟ لا يمكن التراجع عن هذا الإجراء.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("نعم، مسح الكل", color = RedDisconnect, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, KeypadButtonBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = CyanAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    label: String,
    sublabel: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = sublabel, color = TextSecondary, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkBackground,
                checkedTrackColor = CyanPrimary,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkSurfaceVariant
            )
        )
    }
}

@Composable
fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 11.sp)
        Text(text = value, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}
