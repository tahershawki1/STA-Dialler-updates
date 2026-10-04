package com.example.stadialler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stadialler.ui.components.ActiveCallSheet
import com.example.stadialler.ui.screens.ContactsScreen
import com.example.stadialler.ui.screens.DialerScreen
import com.example.stadialler.ui.screens.HistoryScreen
import com.example.stadialler.ui.screens.SettingsScreen
import com.example.stadialler.ui.screens.UpdatesScreen
import com.example.stadialler.ui.theme.AmberUpdate
import com.example.stadialler.ui.theme.CyanAccent
import com.example.stadialler.ui.theme.CyanPrimary
import com.example.stadialler.ui.theme.DarkBackground
import com.example.stadialler.ui.theme.DarkSurface
import com.example.stadialler.ui.theme.STADiallerTheme
import com.example.stadialler.ui.theme.TextMuted
import com.example.stadialler.ui.theme.TextPrimary
import com.example.stadialler.ui.theme.TextSecondary
import com.example.stadialler.viewmodel.CallState
import com.example.stadialler.viewmodel.DialerViewModel

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DIALER("Dialer", Icons.Filled.Dialpad, Icons.Outlined.Dialpad, "tab_dialer"),
    HISTORY("History", Icons.Filled.History, Icons.Outlined.History, "tab_history"),
    CONTACTS("Directory", Icons.Filled.People, Icons.Outlined.People, "tab_contacts"),
    UPDATES("Updates", Icons.Filled.SystemUpdate, Icons.Outlined.SystemUpdate, "tab_updates"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings")
}

class MainActivity : ComponentActivity() {
    private val viewModel: DialerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            STADiallerTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: DialerViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.DIALER) }
    val callState by viewModel.callState.collectAsState()
    val activeNumber by viewModel.activeCallNumber.collectAsState()
    val activeName by viewModel.activeCallName.collectAsState()
    val callDuration by viewModel.callDurationSeconds.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
    val isOnHold by viewModel.isOnHold.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val availableUpdate by viewModel.availableUpdate.collectAsState()

    // Back handling
    if (callState != CallState.IDLE) {
        BackHandler {
            // Cannot dismiss during active call without ending call
            viewModel.endCall()
        }
    } else if (selectedTab != NavigationTab.DIALER) {
        BackHandler {
            selectedTab = NavigationTab.DIALER
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = DarkBackground,
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    NavigationTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        val isUpdatesTab = tab == NavigationTab.UPDATES && availableUpdate != null

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                if (isUpdatesTab) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = AmberUpdate,
                                                modifier = Modifier.testTag("update_nav_badge")
                                            )
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBackground,
                                selectedTextColor = CyanAccent,
                                indicatorColor = CyanPrimary,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    NavigationTab.DIALER -> DialerScreen(
                        viewModel = viewModel,
                        onNavigateToUpdates = { selectedTab = NavigationTab.UPDATES }
                    )
                    NavigationTab.HISTORY -> HistoryScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.CONTACTS -> ContactsScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.UPDATES -> UpdatesScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }

        // Active Call Fullscreen Overlay
        AnimatedVisibility(
            visible = callState != CallState.IDLE,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ActiveCallSheet(
                callState = callState,
                number = activeNumber,
                contactName = activeName,
                durationSeconds = callDuration,
                isMuted = isMuted,
                isSpeakerOn = isSpeakerOn,
                isOnHold = isOnHold,
                isRecording = isRecording,
                onToggleMute = { viewModel.toggleMute() },
                onToggleSpeaker = { viewModel.toggleSpeaker() },
                onToggleHold = { viewModel.toggleHold() },
                onToggleRecord = { viewModel.toggleRecording() },
                onSendDtmf = { viewModel.sendInCallDtmf(it) },
                onEndCall = { viewModel.endCall() }
            )
        }
    }
}
