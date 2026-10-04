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
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.People
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stadialler.ui.components.ActiveCallSheet
import com.example.stadialler.ui.screens.ContactsScreen
import com.example.stadialler.ui.screens.DialerScreen
import com.example.stadialler.ui.screens.HistoryScreen
import com.example.stadialler.ui.screens.SettingsScreen
import com.example.stadialler.ui.screens.UpdatesScreen
import com.example.stadialler.ui.theme.STADiallerTheme
import com.example.stadialler.ui.theme.SamsungDarkBg
import com.example.stadialler.ui.theme.SamsungGreen
import com.example.stadialler.ui.theme.SamsungSurface
import com.example.stadialler.ui.theme.SamsungTextMuted
import com.example.stadialler.ui.theme.SamsungTextSecondary
import com.example.stadialler.viewmodel.CallState
import com.example.stadialler.viewmodel.DialerViewModel

// Samsung One UI Classic 3-Tab Bottom Navigation
enum class SamsungNavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    KEYPAD("لوحة المفاتيح", Icons.Filled.Dialpad, Icons.Outlined.Dialpad, "tab_keypad"),
    RECENTS("الأخيرة", Icons.Filled.History, Icons.Outlined.History, "tab_recents"),
    CONTACTS("جهات الاتصال", Icons.Filled.People, Icons.Outlined.People, "tab_contacts")
}

// Sub-screens opened from Samsung More Options menu
enum class SecondaryScreen {
    NONE,
    SETTINGS,
    UPDATES
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
    var selectedTab by remember { mutableStateOf(SamsungNavigationTab.KEYPAD) }
    var secondaryScreen by remember { mutableStateOf(SecondaryScreen.NONE) }

    val callState by viewModel.callState.collectAsState()
    val activeNumber by viewModel.activeCallNumber.collectAsState()
    val activeName by viewModel.activeCallName.collectAsState()
    val callDuration by viewModel.callDurationSeconds.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
    val isOnHold by viewModel.isOnHold.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()

    // Back handling
    if (callState != CallState.IDLE) {
        BackHandler {
            viewModel.endCall()
        }
    } else if (secondaryScreen != SecondaryScreen.NONE) {
        BackHandler {
            secondaryScreen = SecondaryScreen.NONE
        }
    } else if (selectedTab != SamsungNavigationTab.KEYPAD) {
        BackHandler {
            selectedTab = SamsungNavigationTab.KEYPAD
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = SamsungDarkBg,
            bottomBar = {
                if (secondaryScreen == SecondaryScreen.NONE) {
                    NavigationBar(
                        containerColor = SamsungSurface,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("samsung_bottom_nav_bar")
                    ) {
                        SamsungNavigationTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SamsungGreen,
                                    selectedTextColor = SamsungGreen,
                                    indicatorColor = SamsungGreen.copy(alpha = 0.15f),
                                    unselectedIconColor = SamsungTextMuted,
                                    unselectedTextColor = SamsungTextSecondary
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    secondaryScreen == SecondaryScreen.SETTINGS -> {
                        SettingsScreen(
                            viewModel = viewModel
                        )
                    }
                    secondaryScreen == SecondaryScreen.UPDATES -> {
                        UpdatesScreen(
                            viewModel = viewModel
                        )
                    }
                    else -> {
                        when (selectedTab) {
                            SamsungNavigationTab.KEYPAD -> DialerScreen(
                                viewModel = viewModel,
                                onNavigateToUpdates = { secondaryScreen = SecondaryScreen.UPDATES },
                                onNavigateToSettings = { secondaryScreen = SecondaryScreen.SETTINGS },
                                onNavigateToContacts = { selectedTab = SamsungNavigationTab.CONTACTS }
                            )
                            SamsungNavigationTab.RECENTS -> HistoryScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = { secondaryScreen = SecondaryScreen.SETTINGS }
                            )
                            SamsungNavigationTab.CONTACTS -> ContactsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }

        // Active Call Fullscreen Overlay (Samsung Style)
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
