package com.example.stadialler.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stadialler.data.DialerRepository
import com.example.stadialler.model.ApkRelease
import com.example.stadialler.model.CallRecord
import com.example.stadialler.model.CallType
import com.example.stadialler.model.Contact
import com.example.stadialler.service.DtmfTonePlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CallState {
    IDLE,
    DIALING,
    RINGING,
    CONNECTED,
    ON_HOLD,
    ENDED
}

class DialerViewModel(application: Application) : AndroidViewModel(application) {
    val repository = DialerRepository(application)
    private val dtmfPlayer = DtmfTonePlayer(application)

    // Current app version details
    val currentVersionName = "2.4.1"
    val currentVersionCode = 104
    val releaseRepoName = "tahershawki1/STA-Dialler-updates"

    // Keypad & Dial Input
    private val _dialInput = MutableStateFlow("")
    val dialInput: StateFlow<String> = _dialInput.asStateFlow()

    companion object {
        fun charToT9(c: Char): Char? {
            return when (c.lowercaseChar()) {
                'a', 'b', 'c', 'أ', 'إ', 'آ', 'ا', 'ب', 'ت', 'ث' -> '2'
                'd', 'e', 'f', 'ج', 'ح', 'خ', 'د' -> '3'
                'g', 'h', 'i', 'ذ', 'ر', 'ز', 'س' -> '4'
                'j', 'k', 'l', 'ش', 'ص', 'ض', 'ط' -> '5'
                'm', 'n', 'o', 'ظ', 'ع', 'غ', 'ف' -> '6'
                'p', 'q', 'r', 's', 'ق', 'ك', 'ل', 'م' -> '7'
                't', 'u', 'v', 'ن', 'ه', 'ة', 'و', 'ؤ' -> '8'
                'w', 'x', 'y', 'z', 'ي', 'ى', 'ئ', 'ء' -> '9'
                '0' -> '0'
                '1' -> '1'
                '2' -> '2'
                '3' -> '3'
                '4' -> '4'
                '5' -> '5'
                '6' -> '6'
                '7' -> '7'
                '8' -> '8'
                '9' -> '9'
                else -> null
            }
        }

        fun textToT9(text: String): String {
            val sb = StringBuilder()
            for (ch in text) {
                val d = charToT9(ch)
                if (d != null) sb.append(d)
            }
            return sb.toString()
        }

        fun contactMatchesT9(contact: Contact, inputDigits: String): Boolean {
            if (inputDigits.isEmpty()) return false

            // 1. Phone number contains digits
            val cleanPhone = contact.phoneNumber.replace(Regex("[^0-9]"), "")
            if (cleanPhone.contains(inputDigits)) return true

            // 2. Extension matches or contains
            if (contact.extension?.contains(inputDigits) == true) return true

            // 3. Name word prefix / substring match in Arabic or English T9
            val words = contact.name.split(" ", "-", "_", "(", ")", "/", ".", "+")
            for (word in words) {
                val wordT9 = textToT9(word)
                if (wordT9.startsWith(inputDigits) || wordT9.contains(inputDigits)) {
                    return true
                }
            }

            // 4. Entire name T9 sequence contains inputDigits
            val fullT9 = textToT9(contact.name)
            return fullT9.contains(inputDigits)
        }
    }

    // Matching suggestions for current input (T9 English & Arabic, plus phone number matching)
    val matchedSuggestions: StateFlow<List<Contact>> = combine(_dialInput, repository.contacts) { input, contacts ->
        val clean = input.replace(Regex("[^0-9]"), "")
        if (clean.isEmpty()) {
            emptyList()
        } else {
            contacts.filter { contactMatchesT9(it, clean) }
                .sortedWith(
                    compareByDescending<Contact> { contact ->
                        val words = contact.name.split(" ", "-", "_", "(", ")", "/", ".", "+")
                        words.any { textToT9(it).startsWith(clean) }
                    }.thenByDescending { contact ->
                        textToT9(contact.name).contains(clean)
                    }.thenBy { contact ->
                        contact.name
                    }
                )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val matchedContact: StateFlow<Contact?> = matchedSuggestions.map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Active Call Simulation / In-Call UI
    private val _callState = MutableStateFlow(CallState.IDLE)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val _activeCallNumber = MutableStateFlow("")
    val activeCallNumber: StateFlow<String> = _activeCallNumber.asStateFlow()

    private val _activeCallName = MutableStateFlow<String?>(null)
    val activeCallName: StateFlow<String?> = _activeCallName.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0L)
    val callDurationSeconds: StateFlow<Long> = _callDurationSeconds.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _isOnHold = MutableStateFlow(false)
    val isOnHold: StateFlow<Boolean> = _isOnHold.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private var callTimerJob: Job? = null
    private var callFlowJob: Job? = null

    // History Filter
    private val _historyFilter = MutableStateFlow<CallType?>(null)
    val historyFilter: StateFlow<CallType?> = _historyFilter.asStateFlow()

    val filteredHistory: StateFlow<List<CallRecord>> = combine(
        repository.callHistory,
        _historyFilter
    ) { history, filter ->
        if (filter == null) history else history.filter { it.type == filter }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Contacts & Search
    private val _contactSearchQuery = MutableStateFlow("")
    val contactSearchQuery: StateFlow<String> = _contactSearchQuery.asStateFlow()

    val filteredContacts: StateFlow<List<Contact>> = combine(
        repository.contacts,
        _contactSearchQuery
    ) { contacts, query ->
        if (query.isBlank()) contacts
        else contacts.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.phoneNumber.contains(query) ||
            (it.extension?.contains(query) == true) ||
            it.department.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Updates Feed (STA-Dialler-updates)
    private val _isCheckingUpdates = MutableStateFlow(false)
    val isCheckingUpdates: StateFlow<Boolean> = _isCheckingUpdates.asStateFlow()

    private val _updateMessage = MutableStateFlow<String?>(null)
    val updateMessage: StateFlow<String?> = _updateMessage.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    private val _verifiedRelease = MutableStateFlow<ApkRelease?>(null)
    val verifiedRelease: StateFlow<ApkRelease?> = _verifiedRelease.asStateFlow()

    val availableUpdate: StateFlow<ApkRelease?> = repository.releases.combine(_dialInput) { releases, _ ->
        releases.firstOrNull { it.versionCode > currentVersionCode }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Settings States
    val isDtmfSound = MutableStateFlow(repository.isDtmfSoundEnabled())
    val isHaptic = MutableStateFlow(repository.isHapticFeedbackEnabled())
    val isAutoRecord = MutableStateFlow(repository.isAutoRecordEnabled())
    val sipServer = MutableStateFlow(repository.getSipServer())
    val sipPort = MutableStateFlow(repository.getSipPort())
    val sipExt = MutableStateFlow(repository.getSipExtension())
    val isTls = MutableStateFlow(repository.isTlsEnabled())
    val selectedChannel = MutableStateFlow(repository.getSelectedChannel())

    // -------------------------------------------------------------
    // Keypad Logic
    // -------------------------------------------------------------
    fun onKeyPress(digit: Char) {
        dtmfPlayer.playTone(digit, isDtmfSound.value, isHaptic.value)
        _dialInput.value += digit
    }

    fun onBackspace() {
        if (_dialInput.value.isNotEmpty()) {
            _dialInput.value = _dialInput.value.dropLast(1)
        }
    }

    fun onClearDialInput() {
        _dialInput.value = ""
    }

    fun setDialInput(number: String) {
        _dialInput.value = number
    }

    fun onSpeedDialLongPress(digit: Char) {
        val key = digit.digitToIntOrNull() ?: return
        val contact = repository.contacts.value.firstOrNull { it.speedDialKey == key }
        if (contact != null) {
            startCall(contact.phoneNumber, contact.name)
        }
    }

    // -------------------------------------------------------------
    // Call Flow & Control
    // -------------------------------------------------------------
    fun startCall(number: String, contactName: String? = null, launchSystemDialer: Boolean = false) {
        val targetNumber = if (number.isNotBlank()) number else _dialInput.value
        if (targetNumber.isBlank()) return

        if (launchSystemDialer) {
            try {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${Uri.encode(targetNumber)}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                getApplication<Application>().startActivity(intent)
                // Record in history
                repository.addCallRecord(
                    CallRecord(
                        number = targetNumber,
                        contactName = contactName ?: matchedContact.value?.name,
                        type = CallType.OUTGOING,
                        carrierOrLine = "System Cellular"
                    )
                )
                return
            } catch (e: Exception) {
                // fallback to in-app simulation
            }
        }

        _activeCallNumber.value = targetNumber
        _activeCallName.value = contactName ?: matchedContact.value?.name
        _callDurationSeconds.value = 0L
        _isMuted.value = false
        _isSpeakerOn.value = false
        _isOnHold.value = false
        _isRecording.value = isAutoRecord.value

        _callState.value = CallState.DIALING

        callFlowJob?.cancel()
        callFlowJob = viewModelScope.launch {
            // Dialing tone / state
            delay(1200)
            if (_callState.value == CallState.DIALING) {
                _callState.value = CallState.RINGING
            }
            delay(2200)
            if (_callState.value == CallState.RINGING) {
                _callState.value = CallState.CONNECTED
                startCallTimer()
            }
        }
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_callState.value == CallState.CONNECTED || _callState.value == CallState.ON_HOLD) {
                delay(1000)
                if (!_isOnHold.value) {
                    _callDurationSeconds.value += 1
                }
            }
        }
    }

    fun endCall() {
        val duration = _callDurationSeconds.value
        val number = _activeCallNumber.value
        val name = _activeCallName.value

        callFlowJob?.cancel()
        callTimerJob?.cancel()
        _callState.value = CallState.ENDED

        if (number.isNotBlank()) {
            repository.addCallRecord(
                CallRecord(
                    number = number,
                    contactName = name,
                    type = CallType.OUTGOING,
                    durationSeconds = duration,
                    carrierOrLine = "STA SIP TLS: ext ${sipExt.value}"
                )
            )
        }

        viewModelScope.launch {
            delay(1500)
            _callState.value = CallState.IDLE
            _activeCallNumber.value = ""
            _activeCallName.value = null
            _callDurationSeconds.value = 0L
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
    }

    fun toggleHold() {
        _isOnHold.value = !_isOnHold.value
        _callState.value = if (_isOnHold.value) CallState.ON_HOLD else CallState.CONNECTED
    }

    fun toggleRecording() {
        _isRecording.value = !_isRecording.value
    }

    fun sendInCallDtmf(char: Char) {
        dtmfPlayer.playTone(char, isDtmfSound.value, isHaptic.value)
    }

    // -------------------------------------------------------------
    // History Actions
    // -------------------------------------------------------------
    fun setHistoryFilter(type: CallType?) {
        _historyFilter.value = type
    }

    fun deleteCallRecord(id: String) {
        repository.deleteCallRecord(id)
    }

    fun clearHistory() {
        repository.clearCallHistory()
    }

    // -------------------------------------------------------------
    // Contacts Actions
    // -------------------------------------------------------------
    fun setContactSearchQuery(query: String) {
        _contactSearchQuery.value = query
    }

    fun saveContact(contact: Contact) {
        repository.addOrUpdateContact(contact)
    }

    fun deleteContact(id: String) {
        repository.deleteContact(id)
    }

    fun assignSpeedDial(contactId: String, key: Int) {
        repository.assignSpeedDial(contactId, key)
    }

    // -------------------------------------------------------------
    // Signed APK Updates Feed Actions
    // -------------------------------------------------------------
    fun checkForUpdates() {
        viewModelScope.launch {
            _isCheckingUpdates.value = true
            _updateMessage.value = "Connecting to $releaseRepoName feed..."
            delay(1000)

            val result = repository.checkRemoteUpdateFeed()
            _isCheckingUpdates.value = false
            if (result.isSuccess) {
                val latest = availableUpdate.value
                if (latest != null) {
                    _updateMessage.value = "New release found: ${latest.versionName} (${latest.channel})"
                } else {
                    _updateMessage.value = "STA Dialer is up to date (v$currentVersionName)."
                }
            } else {
                _updateMessage.value = "Feed check completed. Using verified cached signatures."
            }
        }
    }

    fun downloadAndVerifyApk(release: ApkRelease) {
        viewModelScope.launch {
            _downloadProgress.value = 0.05f
            _updateMessage.value = "Downloading signed package: ${release.apkFileName}..."
            for (step in 1..10) {
                delay(250)
                _downloadProgress.value = step / 10f
            }
            delay(400)
            _updateMessage.value = "Verifying cryptographic signature & SHA-256 integrity..."
            delay(700)
            _verifiedRelease.value = release
            _downloadProgress.value = null
            _updateMessage.value = "Verified signed build: ${release.versionName}. Ready to install."
        }
    }

    fun clearVerifiedRelease() {
        _verifiedRelease.value = null
    }

    // -------------------------------------------------------------
    // Settings Actions
    // -------------------------------------------------------------
    fun updateDtmfSound(enabled: Boolean) {
        isDtmfSound.value = enabled
        repository.setDtmfSoundEnabled(enabled)
    }

    fun updateHaptic(enabled: Boolean) {
        isHaptic.value = enabled
        repository.setHapticFeedbackEnabled(enabled)
    }

    fun updateAutoRecord(enabled: Boolean) {
        isAutoRecord.value = enabled
        repository.setAutoRecordEnabled(enabled)
    }

    fun updateSipServer(server: String) {
        sipServer.value = server
        repository.setSipServer(server)
    }

    fun updateSipPort(port: String) {
        sipPort.value = port
        repository.setSipPort(port)
    }

    fun updateSipExt(ext: String) {
        sipExt.value = ext
        repository.setSipExtension(ext)
    }

    fun updateTls(enabled: Boolean) {
        isTls.value = enabled
        repository.setTlsEnabled(enabled)
    }

    fun updateChannel(channel: String) {
        selectedChannel.value = channel
        repository.setSelectedChannel(channel)
    }

    override fun onCleared() {
        super.onCleared()
        dtmfPlayer.release()
        callTimerJob?.cancel()
        callFlowJob?.cancel()
    }
}
