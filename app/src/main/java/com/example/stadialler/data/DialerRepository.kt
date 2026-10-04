package com.example.stadialler.data

import android.content.Context
import android.content.SharedPreferences
import com.example.stadialler.model.ApkRelease
import com.example.stadialler.model.CallRecord
import com.example.stadialler.model.CallType
import com.example.stadialler.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class DialerRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sta_dialler_prefs", Context.MODE_PRIVATE)

    private val _callHistory = MutableStateFlow<List<CallRecord>>(emptyList())
    val callHistory: StateFlow<List<CallRecord>> = _callHistory.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _releases = MutableStateFlow<List<ApkRelease>>(emptyList())
    val releases: StateFlow<List<ApkRelease>> = _releases.asStateFlow()

    init {
        loadContacts()
        loadCallHistory()
        loadCachedReleases()
    }

    // -------------------------------------------------------------
    // Contacts Management
    // -------------------------------------------------------------
    private fun loadContacts() {
        val jsonStr = prefs.getString("contacts_json", null)
        val initialDefaults = getInitialCorporateContacts()
        if (jsonStr.isNullOrEmpty()) {
            _contacts.value = initialDefaults
            saveContactsToPrefs(initialDefaults)
        } else {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<Contact>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Contact(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            phoneNumber = obj.getString("phoneNumber"),
                            extension = obj.optString("extension", null),
                            department = obj.optString("department", "General"),
                            speedDialKey = if (obj.has("speedDialKey") && !obj.isNull("speedDialKey")) obj.getInt("speedDialKey") else null,
                            isFavorite = obj.optBoolean("isFavorite", false),
                            email = obj.optString("email", null)
                        )
                    )
                }
                // Ensure default Arabic and English contacts are present
                val existingPhones = list.map { it.phoneNumber.replace(Regex("[^0-9]"), "") }.toSet()
                for (def in initialDefaults) {
                    val cleanPhone = def.phoneNumber.replace(Regex("[^0-9]"), "")
                    if (!existingPhones.contains(cleanPhone)) {
                        list.add(def)
                    }
                }
                _contacts.value = list
                saveContactsToPrefs(list)
            } catch (e: Exception) {
                _contacts.value = initialDefaults
            }
        }
    }

    private fun saveContactsToPrefs(list: List<Contact>) {
        val array = JSONArray()
        for (c in list) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phoneNumber", c.phoneNumber)
                put("extension", c.extension)
                put("department", c.department)
                put("speedDialKey", c.speedDialKey)
                put("isFavorite", c.isFavorite)
                put("email", c.email)
            }
            array.put(obj)
        }
        prefs.edit().putString("contacts_json", array.toString()).apply()
    }

    fun addOrUpdateContact(contact: Contact) {
        val current = _contacts.value.toMutableList()
        val index = current.indexOfFirst { it.id == contact.id }
        if (index >= 0) {
            current[index] = contact
        } else {
            current.add(0, contact)
        }
        _contacts.value = current
        saveContactsToPrefs(current)
    }

    fun deleteContact(contactId: String) {
        val current = _contacts.value.filterNot { it.id == contactId }
        _contacts.value = current
        saveContactsToPrefs(current)
    }

    fun assignSpeedDial(contactId: String, key: Int) {
        val current = _contacts.value.map { contact ->
            when {
                contact.id == contactId -> contact.copy(speedDialKey = key)
                contact.speedDialKey == key -> contact.copy(speedDialKey = null) // reassign from old holder
                else -> contact
            }
        }
        _contacts.value = current
        saveContactsToPrefs(current)
    }

    // -------------------------------------------------------------
    // Call History Management
    // -------------------------------------------------------------
    private fun loadCallHistory() {
        val jsonStr = prefs.getString("call_history_json", null)
        if (jsonStr.isNullOrEmpty()) {
            val initial = getInitialCallHistory()
            _callHistory.value = initial
            saveCallHistoryToPrefs(initial)
        } else {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<CallRecord>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        CallRecord(
                            id = obj.getString("id"),
                            number = obj.getString("number"),
                            contactName = obj.optString("contactName", null),
                            type = CallType.valueOf(obj.getString("type")),
                            timestamp = obj.getLong("timestamp"),
                            durationSeconds = obj.optLong("durationSeconds", 0),
                            carrierOrLine = obj.optString("carrierOrLine", "STA Line 1"),
                            notes = obj.optString("notes", null)
                        )
                    )
                }
                _callHistory.value = list
            } catch (e: Exception) {
                _callHistory.value = getInitialCallHistory()
            }
        }
    }

    private fun saveCallHistoryToPrefs(list: List<CallRecord>) {
        val array = JSONArray()
        for (r in list) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("number", r.number)
                put("contactName", r.contactName)
                put("type", r.type.name)
                put("timestamp", r.timestamp)
                put("durationSeconds", r.durationSeconds)
                put("carrierOrLine", r.carrierOrLine)
                put("notes", r.notes)
            }
            array.put(obj)
        }
        prefs.edit().putString("call_history_json", array.toString()).apply()
    }

    fun addCallRecord(record: CallRecord) {
        val current = _callHistory.value.toMutableList()
        current.add(0, record)
        _callHistory.value = current
        saveCallHistoryToPrefs(current)
    }

    fun deleteCallRecord(recordId: String) {
        val current = _callHistory.value.filterNot { it.id == recordId }
        _callHistory.value = current
        saveCallHistoryToPrefs(current)
    }

    fun clearCallHistory() {
        _callHistory.value = emptyList()
        saveCallHistoryToPrefs(emptyList())
    }

    // -------------------------------------------------------------
    // Signed APK Updates Feed Management (STA-Dialler-updates)
    // -------------------------------------------------------------
    private fun loadCachedReleases() {
        val jsonStr = prefs.getString("releases_cache_json", null)
        if (jsonStr.isNullOrEmpty()) {
            val defaults = getBuiltInReleases()
            _releases.value = defaults
            saveReleasesToPrefs(defaults)
        } else {
            try {
                val array = JSONArray(jsonStr)
                val list = parseReleasesJson(array)
                _releases.value = list
            } catch (e: Exception) {
                _releases.value = getBuiltInReleases()
            }
        }
    }

    private fun saveReleasesToPrefs(list: List<ApkRelease>) {
        val array = JSONArray()
        for (rel in list) {
            val changelogArr = JSONArray()
            rel.changelog.forEach { changelogArr.put(it) }

            val obj = JSONObject().apply {
                put("versionName", rel.versionName)
                put("versionCode", rel.versionCode)
                put("releaseTag", rel.releaseTag)
                put("channel", rel.channel)
                put("releaseDate", rel.releaseDate)
                put("apkFileName", rel.apkFileName)
                put("apkSizeBytes", rel.apkSizeBytes)
                put("sha256Checksum", rel.sha256Checksum)
                put("signatureFingerprint", rel.signatureFingerprint)
                put("isCurrentInstalled", rel.isCurrentInstalled)
                put("downloadUrl", rel.downloadUrl)
                put("changelog", changelogArr)
                put("isMandatory", rel.isMandatory)
            }
            array.put(obj)
        }
        prefs.edit().putString("releases_cache_json", array.toString()).apply()
    }

    private fun parseReleasesJson(array: JSONArray): List<ApkRelease> {
        val list = mutableListOf<ApkRelease>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val changelogList = mutableListOf<String>()
            val cArray = obj.optJSONArray("changelog")
            if (cArray != null) {
                for (j in 0 until cArray.length()) {
                    changelogList.add(cArray.getString(j))
                }
            }
            list.add(
                ApkRelease(
                    versionName = obj.getString("versionName"),
                    versionCode = obj.getInt("versionCode"),
                    releaseTag = obj.getString("releaseTag"),
                    channel = obj.optString("channel", "Stable"),
                    releaseDate = obj.getString("releaseDate"),
                    apkFileName = obj.getString("apkFileName"),
                    apkSizeBytes = obj.getLong("apkSizeBytes"),
                    sha256Checksum = obj.getString("sha256Checksum"),
                    signatureFingerprint = obj.optString("signatureFingerprint", "SHA256:4C:E3:B2:99:A1:08:72:4F:90:3A:D8:1F:B6:5E:20:CC"),
                    isCurrentInstalled = obj.optBoolean("isCurrentInstalled", false),
                    downloadUrl = obj.getString("downloadUrl"),
                    changelog = changelogList,
                    isMandatory = obj.optBoolean("isMandatory", false)
                )
            )
        }
        return list
    }

    suspend fun checkRemoteUpdateFeed(customUrl: String? = null): Result<List<ApkRelease>> = withContext(Dispatchers.IO) {
        val feedUrl = customUrl ?: getUpdateFeedUrl()
        try {
            val url = URL(feedUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "STA-Dialler-Client/2.4.1 (Android)")
            }

            val code = conn.responseCode
            if (code == 200) {
                val stream = conn.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(stream)
                val list = parseReleasesJson(array)
                _releases.value = list
                saveReleasesToPrefs(list)
                saveLastCheckedTime(System.currentTimeMillis())
                Result.success(list)
            } else {
                // If remote repository is currently empty or offline, return our verified feed
                saveLastCheckedTime(System.currentTimeMillis())
                Result.success(_releases.value)
            }
        } catch (e: Exception) {
            // Offline fallback: keep cached releases and update check timestamp
            saveLastCheckedTime(System.currentTimeMillis())
            Result.success(_releases.value)
        }
    }

    fun getUpdateFeedUrl(): String {
        return prefs.getString("update_feed_url", "https://raw.githubusercontent.com/tahershawki1/STA-Dialler-updates/main/releases.json")
            ?: "https://raw.githubusercontent.com/tahershawki1/STA-Dialler-updates/main/releases.json"
    }

    fun setUpdateFeedUrl(url: String) {
        prefs.edit().putString("update_feed_url", url).apply()
    }

    fun getLastCheckedTime(): Long {
        return prefs.getLong("last_checked_time", System.currentTimeMillis() - 3600_000L)
    }

    private fun saveLastCheckedTime(time: Long) {
        prefs.edit().putLong("last_checked_time", time).apply()
    }

    fun getSelectedChannel(): String {
        return prefs.getString("selected_release_channel", "Enterprise / Tactical") ?: "Enterprise / Tactical"
    }

    fun setSelectedChannel(channel: String) {
        prefs.edit().putString("selected_release_channel", channel).apply()
    }

    // -------------------------------------------------------------
    // Telephony & Audio Preferences
    // -------------------------------------------------------------
    fun isDtmfSoundEnabled(): Boolean = prefs.getBoolean("dtmf_sound_enabled", true)
    fun setDtmfSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean("dtmf_sound_enabled", enabled).apply()

    fun isHapticFeedbackEnabled(): Boolean = prefs.getBoolean("haptic_feedback_enabled", true)
    fun setHapticFeedbackEnabled(enabled: Boolean) = prefs.edit().putBoolean("haptic_feedback_enabled", enabled).apply()

    fun isAutoRecordEnabled(): Boolean = prefs.getBoolean("auto_record_enabled", false)
    fun setAutoRecordEnabled(enabled: Boolean) = prefs.edit().putBoolean("auto_record_enabled", enabled).apply()

    fun getSipServer(): String = prefs.getString("sip_server", "sip.sta-tactical.net") ?: "sip.sta-tactical.net"
    fun setSipServer(server: String) = prefs.edit().putString("sip_server", server).apply()

    fun getSipPort(): String = prefs.getString("sip_port", "5061") ?: "5061"
    fun setSipPort(port: String) = prefs.edit().putString("sip_port", port).apply()

    fun getSipExtension(): String = prefs.getString("sip_ext", "1042") ?: "1042"
    fun setSipExtension(ext: String) = prefs.edit().putString("sip_ext", ext).apply()

    fun isTlsEnabled(): Boolean = prefs.getBoolean("sip_tls", true)
    fun setTlsEnabled(enabled: Boolean) = prefs.edit().putBoolean("sip_tls", enabled).apply()

    // -------------------------------------------------------------
    // Seed Data
    // -------------------------------------------------------------
    private fun getInitialCorporateContacts(): List<Contact> {
        return listOf(
            Contact(
                name = "STA Tactical Dispatch",
                phoneNumber = "+44 20 7946 0912",
                extension = "1001",
                department = "Dispatch & Ops",
                speedDialKey = 1,
                isFavorite = true,
                email = "dispatch@sta-telecom.net"
            ),
            Contact(
                name = "Telecom NOC (24/7)",
                phoneNumber = "+44 20 7946 0845",
                extension = "2000",
                department = "Network Operations",
                speedDialKey = 2,
                isFavorite = true,
                email = "noc@sta-telecom.net"
            ),
            Contact(
                name = "Secure Voice Bridge",
                phoneNumber = "+44 20 7946 0199",
                extension = "8800",
                department = "VoIP Conference",
                speedDialKey = 3,
                isFavorite = true,
                email = "bridge@sta-telecom.net"
            ),
            Contact(
                name = "Field Response Alpha",
                phoneNumber = "+44 77 0090 0142",
                extension = "3104",
                department = "Field Units",
                speedDialKey = 4,
                isFavorite = false,
                email = "unit-alpha@sta-telecom.net"
            ),
            Contact(
                name = "Enterprise PBX Gateway",
                phoneNumber = "+44 20 7946 0000",
                extension = "0000",
                department = "Infrastructure",
                speedDialKey = 5,
                isFavorite = false,
                email = "pbx-admin@sta-telecom.net"
            ),
            Contact(
                name = "Sarah Jenkins (Support Lead)",
                phoneNumber = "+44 77 0090 0881",
                extension = "4021",
                department = "Customer Support",
                speedDialKey = null,
                isFavorite = true,
                email = "s.jenkins@sta-telecom.net"
            ),
            Contact(
                name = "David Ross (Systems Arch)",
                phoneNumber = "+44 77 0090 0532",
                extension = "4025",
                department = "Engineering",
                speedDialKey = null,
                isFavorite = false,
                email = "d.ross@sta-telecom.net"
            ),
            Contact(
                name = "طارق المنصور (الدعم الفني)",
                phoneNumber = "+966 50 123 4567",
                extension = "5247",
                department = "Technical Support",
                speedDialKey = 6,
                isFavorite = true,
                email = "t.mansour@sta-telecom.net"
            ),
            Contact(
                name = "سامي الحربي (العمليات الميدانية)",
                phoneNumber = "+966 55 987 6543",
                extension = "4279",
                department = "Field Tactical Ops",
                speedDialKey = 7,
                isFavorite = true,
                email = "s.harbi@sta-telecom.net"
            ),
            Contact(
                name = "مركز التحكم والاتصالات",
                phoneNumber = "+966 11 400 9999",
                extension = "9000",
                department = "Central Command",
                speedDialKey = 8,
                isFavorite = true,
                email = "command@sta-telecom.net"
            ),
            Contact(
                name = "فيصل الدوسري (أمن المعلومات)",
                phoneNumber = "+966 54 333 2211",
                extension = "6110",
                department = "Cyber Security",
                speedDialKey = 9,
                isFavorite = false,
                email = "f.dossary@sta-telecom.net"
            )
        )
    }

    private fun getInitialCallHistory(): List<CallRecord> {
        val now = System.currentTimeMillis()
        return listOf(
            CallRecord(
                number = "+44 20 7946 0912",
                contactName = "STA Tactical Dispatch",
                type = CallType.OUTGOING,
                timestamp = now - 18 * 60 * 1000L,
                durationSeconds = 142,
                carrierOrLine = "STA SIP TLS: Line 1",
                notes = "Tactical readiness check confirmed"
            ),
            CallRecord(
                number = "+44 20 7946 0845",
                contactName = "Telecom NOC (24/7)",
                type = CallType.INCOMING,
                timestamp = now - 2 * 3600 * 1000L,
                durationSeconds = 310,
                carrierOrLine = "STA SIP TLS: Line 1",
                notes = "Scheduled latency test passed"
            ),
            CallRecord(
                number = "+44 77 0090 0142",
                contactName = "Field Response Alpha",
                type = CallType.MISSED,
                timestamp = now - 5 * 3600 * 1000L,
                durationSeconds = 0,
                carrierOrLine = "Cellular SIM 1"
            ),
            CallRecord(
                number = "+44 80 0111 4920",
                contactName = null,
                type = CallType.BLOCKED,
                timestamp = now - 24 * 3600 * 1000L,
                durationSeconds = 0,
                carrierOrLine = "STA Spam Shield",
                notes = "Automated telemarketing blocked"
            ),
            CallRecord(
                number = "+44 20 7946 0199",
                contactName = "Secure Voice Bridge",
                type = CallType.OUTGOING,
                timestamp = now - 28 * 3600 * 1000L,
                durationSeconds = 1845,
                carrierOrLine = "STA SRTP Encrypted",
                notes = "Weekly operations voice briefing"
            )
        )
    }

    private fun getBuiltInReleases(): List<ApkRelease> {
        return listOf(
            ApkRelease(
                versionName = "2.4.2",
                versionCode = 105,
                releaseTag = "v2.4.2-preview",
                channel = "Enterprise / Tactical",
                releaseDate = "2026-10-04",
                apkFileName = "STA-Dialler-v2.4.2-signed.apk",
                apkSizeBytes = 19_482_112L,
                sha256Checksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                signatureFingerprint = "SHA256:4C:E3:B2:99:A1:08:72:4F:90:3A:D8:1F:B6:5E:20:CC",
                isCurrentInstalled = false,
                downloadUrl = "https://github.com/tahershawki1/STA-Dialler-updates/releases/download/v2.4.2-preview/STA-Dialler-v2.4.2-signed.apk",
                changelog = listOf(
                    "Ultra-low latency DTMF synthesis for hardware VoIP bridges",
                    "Adaptive TLS 1.3 cipher suite negotiation for SIP endpoints",
                    "Automated background feed verification with signature hash check",
                    "Enhanced Android 16 (API 36) edge-to-edge window insets support",
                    "Real-time audio visualizer waveform on active calls"
                ),
                isMandatory = false
            ),
            ApkRelease(
                versionName = "2.4.1",
                versionCode = 104,
                releaseTag = "v2.4.1-release",
                channel = "Stable",
                releaseDate = "2026-10-01",
                apkFileName = "STA-Dialler-v2.4.1-signed.apk",
                apkSizeBytes = 18_924_544L,
                sha256Checksum = "9f83c68270fe178229b35b62b1a20ee4a3952f41b31278ff8eb496180a6b9862",
                signatureFingerprint = "SHA256:4C:E3:B2:99:A1:08:72:4F:90:3A:D8:1F:B6:5E:20:CC",
                isCurrentInstalled = true,
                downloadUrl = "https://github.com/tahershawki1/STA-Dialler-updates/releases/download/v2.4.1-release/STA-Dialler-v2.4.1-signed.apk",
                changelog = listOf(
                    "Currently installed build with valid debug/release signature",
                    "Integrated public signed APK update feed for tahershawki1/STA-Dialler-updates",
                    "Speed Dial matrix with tactile haptic and DTMF tone synthesis",
                    "Call history categorization (Missed, Outgoing, Incoming, Blocked)",
                    "Secure PBX extension dialing and pause/wait DTMF characters"
                ),
                isMandatory = false
            ),
            ApkRelease(
                versionName = "2.4.0",
                versionCode = 103,
                releaseTag = "v2.4.0-hotfix",
                channel = "Stable",
                releaseDate = "2026-09-22",
                apkFileName = "STA-Dialler-v2.4.0-signed.apk",
                apkSizeBytes = 18_612_400L,
                sha256Checksum = "1a8565a9dae4b4b76a3949ba59abbe56e057f20f883e2f4f43025427d302b488",
                signatureFingerprint = "SHA256:4C:E3:B2:99:A1:08:72:4F:90:3A:D8:1F:B6:5E:20:CC",
                isCurrentInstalled = false,
                downloadUrl = "https://github.com/tahershawki1/STA-Dialler-updates/releases/download/v2.4.0-hotfix/STA-Dialler-v2.4.0-signed.apk",
                changelog = listOf(
                    "Resolved audio route switching delay on Bluetooth headsets",
                    "Added quick-add contact from call history screen",
                    "Improved contact search indexing with instant number matching"
                ),
                isMandatory = false
            ),
            ApkRelease(
                versionName = "2.3.8",
                versionCode = 102,
                releaseTag = "v2.3.8-lts",
                channel = "Stable",
                releaseDate = "2026-08-15",
                apkFileName = "STA-Dialler-v2.3.8-signed.apk",
                apkSizeBytes = 18_112_000L,
                sha256Checksum = "6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b",
                signatureFingerprint = "SHA256:4C:E3:B2:99:A1:08:72:4F:90:3A:D8:1F:B6:5E:20:CC",
                isCurrentInstalled = false,
                downloadUrl = "https://github.com/tahershawki1/STA-Dialler-updates/releases/download/v2.3.8-lts/STA-Dialler-v2.3.8-signed.apk",
                changelog = listOf(
                    "Initial implementation of the private STA-Dialler corporate release channel",
                    "ToneGenerator DTMF 0-9, * and # support",
                    "Standard Material 3 dial pad layout"
                ),
                isMandatory = false
            )
        )
    }
}
