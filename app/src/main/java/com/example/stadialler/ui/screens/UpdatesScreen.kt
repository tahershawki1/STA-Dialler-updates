package com.example.stadialler.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stadialler.model.ApkRelease
import com.example.stadialler.ui.theme.AmberUpdate
import com.example.stadialler.ui.theme.CyanAccent
import com.example.stadialler.ui.theme.CyanLight
import com.example.stadialler.ui.theme.CyanPrimary
import com.example.stadialler.ui.theme.DarkBackground
import com.example.stadialler.ui.theme.DarkSurface
import com.example.stadialler.ui.theme.DarkSurfaceElevated
import com.example.stadialler.ui.theme.DarkSurfaceVariant
import com.example.stadialler.ui.theme.GreenConnect
import com.example.stadialler.ui.theme.KeypadButtonBorder
import com.example.stadialler.ui.theme.TextMuted
import com.example.stadialler.ui.theme.TextPrimary
import com.example.stadialler.ui.theme.TextSecondary
import com.example.stadialler.viewmodel.DialerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UpdatesScreen(
    viewModel: DialerViewModel,
    modifier: Modifier = Modifier
) {
    val releases by viewModel.repository.releases.collectAsState()
    val isChecking by viewModel.isCheckingUpdates.collectAsState()
    val updateMessage by viewModel.updateMessage.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val verifiedRelease by viewModel.verifiedRelease.collectAsState()
    val availableUpdate by viewModel.availableUpdate.collectAsState()
    val selectedChannel by viewModel.selectedChannel.collectAsState()

    val lastChecked = remember(isChecking) {
        val time = viewModel.repository.getLastCheckedTime()
        SimpleDateFormat("HH:mm:ss, MMM d", Locale.getDefault()).format(Date(time))
    }

    var selectedReleaseForDetails by remember { mutableStateOf<ApkRelease?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("updates_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "APK UPDATE FEED",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Source: ${viewModel.releaseRepoName}",
                        color = CyanAccent,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.checkForUpdates() },
                    enabled = !isChecking,
                    modifier = Modifier.testTag("check_updates_icon_button")
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            color = CyanPrimary,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Check for Updates",
                            tint = CyanPrimary
                        )
                    }
                }
            }
        }

        // Current Installed Build Card
        item {
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GreenConnect.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified Build",
                                    tint = GreenConnect,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "INSTALLED BUILD",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "STA Dialer v${viewModel.currentVersionName} (Build ${viewModel.currentVersionCode})",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = selectedChannel,
                                color = CyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Release Signing Key:",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "RSA-4096 (Signed APK Feed)",
                            color = GreenConnect,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Last Feed Check:",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = lastChecked,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    if (updateMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = updateMessage ?: "",
                            color = CyanLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Available Update Alert Banner (if newer build exists)
        if (availableUpdate != null) {
            item {
                val update = availableUpdate!!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AmberUpdate.copy(alpha = 0.12f))
                        .border(1.5.dp, AmberUpdate, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .testTag("available_update_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    tint = AmberUpdate,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "NEW SIGNED APK READY",
                                        color = AmberUpdate,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Version ${update.versionName} • ${update.formattedSize}",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmberUpdate)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = update.channel,
                                    color = DarkBackground,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Release Tag: ${update.releaseTag} (${update.releaseDate})",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Highlights
                        update.changelog.take(2).forEach { note ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FiberManualRecord,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(6.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = note,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Download Progress bar
                        if (downloadProgress != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress ?: 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AmberUpdate,
                                trackColor = DarkSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Downloading & Verifying: ${(downloadProgress!! * 100).toInt()}%",
                                color = AmberUpdate,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.downloadAndVerifyApk(update) },
                                enabled = downloadProgress == null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberUpdate,
                                    contentColor = DarkBackground
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("download_verify_apk_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (downloadProgress != null) "Verifying..." else "Download & Verify",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { selectedReleaseForDetails = update },
                                modifier = Modifier.testTag("inspect_release_button")
                            ) {
                                Text("Details", color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Release Feed History Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FEED RELEASES ARCHIVE",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${releases.size} signed packages",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // List of all releases in feed
        items(releases) { release ->
            ReleaseFeedItem(
                release = release,
                onViewDetails = { selectedReleaseForDetails = release },
                onVerifyClick = { viewModel.downloadAndVerifyApk(release) }
            )
        }
    }

    // Modal Dialog: Detailed Release & Cryptographic Signature Inspector
    selectedReleaseForDetails?.let { release ->
        ReleaseDetailsDialog(
            release = release,
            onDismiss = { selectedReleaseForDetails = null },
            onDownloadVerify = {
                viewModel.downloadAndVerifyApk(release)
                selectedReleaseForDetails = null
            }
        )
    }

    // Modal Dialog: Verified Build Ready
    verifiedRelease?.let { release ->
        AlertDialog(
            onDismissRequest = { viewModel.clearVerifiedRelease() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenConnect)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Signature Verified", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Package ${release.apkFileName} successfully downloaded and verified with official STA release certificates.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(text = "SHA-256 Checksum:", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = release.sha256Checksum,
                                color = CyanAccent,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Text(
                        text = "In a deployment environment, Android Package Installer will replace the current build.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearVerifiedRelease() },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenConnect, contentColor = DarkBackground)
                ) {
                    Text("Ready", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ReleaseFeedItem(
    release: ApkRelease,
    onViewDetails: () -> Unit,
    onVerifyClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, KeypadButtonBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onViewDetails)
            .padding(14.dp)
            .testTag("release_item_${release.versionName}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "v${release.versionName}",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (release.isCurrentInstalled) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GreenConnect.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CURRENT",
                                color = GreenConnect,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = release.releaseDate,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${release.apkFileName} • ${release.formattedSize} • ${release.channel}",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = release.changelog.firstOrNull() ?: "Release update",
                color = TextMuted,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ReleaseDetailsDialog(
    release: ApkRelease,
    onDismiss: () -> Unit,
    onDownloadVerify: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Release: v${release.versionName}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = release.releaseTag,
                    color = CyanAccent,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "CHANGELOG:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                release.changelog.forEach { item ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = "• ", color = CyanPrimary, fontWeight = FontWeight.Bold)
                        Text(text = item, color = TextPrimary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(text = "CRYPTOGRAPHIC INTEGRITY:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "File: ${release.apkFileName}", color = TextSecondary, fontSize = 10.sp)
                        Text(text = "Size: ${release.formattedSize}", color = TextSecondary, fontSize = 10.sp)
                        Text(text = "Signing Fingerprint:", color = TextMuted, fontSize = 9.sp)
                        Text(
                            text = release.signatureFingerprint,
                            color = GreenConnect,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(text = "SHA-256 Checksum:", color = TextMuted, fontSize = 9.sp)
                        Text(
                            text = release.sha256Checksum,
                            color = CyanAccent,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDownloadVerify,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = DarkBackground)
            ) {
                Text("Verify Build", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
