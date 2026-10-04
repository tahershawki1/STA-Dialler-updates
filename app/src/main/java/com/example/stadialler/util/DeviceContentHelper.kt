package com.example.stadialler.util

import android.Manifest
import android.content.ContentProviderOperation
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.stadialler.model.CallRecord
import com.example.stadialler.model.CallType
import com.example.stadialler.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DeviceContentHelper {

    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCallLogPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun fetchDeviceContacts(context: Context): List<Contact> = withContext(Dispatchers.IO) {
        if (!hasContactsPermission(context)) return@withContext emptyList()

        val contactsList = mutableListOf<Contact>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.STARRED
        )
        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val starredIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)

                val seenNumbers = mutableSetOf<String>()

                while (it.moveToNext()) {
                    val id = if (idIdx != -1) it.getString(idIdx) else java.util.UUID.randomUUID().toString()
                    val name = if (nameIdx != -1) it.getString(nameIdx) ?: "بدون اسم" else "بدون اسم"
                    val number = if (numIdx != -1) it.getString(numIdx) ?: "" else ""
                    val isStarred = if (starredIdx != -1) it.getInt(starredIdx) == 1 else false

                    val cleanNum = number.replace(Regex("[^0-9+]"), "")
                    if (cleanNum.isNotEmpty() && seenNumbers.add(cleanNum)) {
                        contactsList.add(
                            Contact(
                                id = id,
                                name = name,
                                phoneNumber = number,
                                isFavorite = isStarred
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        contactsList
    }

    suspend fun fetchDeviceCallLog(context: Context): List<CallRecord> = withContext(Dispatchers.IO) {
        if (!hasCallLogPermission(context)) return@withContext emptyList()

        val records = mutableListOf<CallRecord>()
        val uri = CallLog.Calls.CONTENT_URI
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )
        val sortOrder = "${CallLog.Calls.DATE} DESC LIMIT 150"

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(CallLog.Calls._ID)
                val numIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIdx = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
                val durIdx = it.getColumnIndex(CallLog.Calls.DURATION)

                while (it.moveToNext()) {
                    val id = if (idIdx != -1) it.getString(idIdx) else java.util.UUID.randomUUID().toString()
                    val number = if (numIdx != -1) it.getString(numIdx) ?: "" else ""
                    val cachedName = if (nameIdx != -1) it.getString(nameIdx) else null
                    val rawType = if (typeIdx != -1) it.getInt(typeIdx) else CallLog.Calls.INCOMING_TYPE
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else System.currentTimeMillis()
                    val duration = if (durIdx != -1) it.getLong(durIdx) else 0L

                    val callType = when (rawType) {
                        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                        CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
                        else -> CallType.INCOMING
                    }

                    records.add(
                        CallRecord(
                            id = id,
                            number = number,
                            contactName = cachedName,
                            type = callType,
                            timestamp = date,
                            durationSeconds = duration,
                            carrierOrLine = "SIM 1"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        records
    }

    suspend fun saveContactToDevice(context: Context, name: String, phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        val hasWrite = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasWrite) return@withContext false

        try {
            val ops = ArrayList<ContentProviderOperation>()

            // Raw Contact insert
            val rawContactInsertIndex = ops.size
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            // Name insert
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build()
            )

            // Phone insert
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            )

            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
