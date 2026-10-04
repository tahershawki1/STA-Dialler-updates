package com.example.stadialler.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.stadialler.model.Contact
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

@Composable
fun ContactsScreen(
    viewModel: DialerViewModel,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.filteredContacts.collectAsState()
    val searchQuery by viewModel.contactSearchQuery.collectAsState()
    val hasContactsPermission by viewModel.hasContactsPermission.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val context = LocalContext.current

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.updatePermissionStates(context)
        }
    }

    var isSearchActive by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SamsungDarkBg)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("contacts_screen")
    ) {
        // 1. Samsung One UI Large Header & Top Actions: [Search] [+] [3-Dots]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "جهات الاتصال",
                    color = SamsungTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${contacts.size} جهة اتصال",
                    color = SamsungTextSecondary,
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { isSearchActive = !isSearchActive },
                    modifier = Modifier.size(40.dp).testTag("contacts_search_toggle")
                ) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = SamsungTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(40.dp).testTag("add_contact_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة جهة اتصال",
                        tint = SamsungTextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // Search Input Field
        if (isSearchActive) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setContactSearchQuery(it) },
                placeholder = { Text("بحث في الأسماء أو الأرقام...", color = SamsungTextMuted) },
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
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setContactSearchQuery("") }) {
                            Icon(Icons.Default.Close, "مسح", tint = SamsungTextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("contacts_search_field")
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (!hasContactsPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SamsungGreen.copy(alpha = 0.12f))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "عرض أسماء هاتفك",
                            color = SamsungTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "اضغط للموافقة على قراءة جهات الاتصال المسجلة على هذا الهاتف.",
                            color = SamsungTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            contactsPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SamsungGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("السماح", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Samsung "My Profile" Item
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SamsungSurface)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SamsungSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "الملف الشخصي",
                            tint = SamsungBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "ملفي الشخصي",
                            color = SamsungTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "STA SIP User (Ext ${viewModel.sipExt.value})",
                            color = SamsungTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Favorites Section
            val favorites = contacts.filter { it.isFavorite }
            if (favorites.isNotEmpty()) {
                item {
                    Text(
                        text = "المفضلة",
                        color = SamsungTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp, top = 8.dp, bottom = 2.dp)
                    )
                }

                items(favorites, key = { "fav_${it.id}" }) { contact ->
                    SamsungContactCard(
                        contact = contact,
                        onCall = { viewModel.makeRealCall(context, contact.phoneNumber, contact.name) },
                        onMessage = { viewModel.sendSms(context, contact.phoneNumber) },
                        onToggleFavorite = { viewModel.toggleFavorite(contact.id) },
                        onDelete = { viewModel.deleteContact(contact.id) }
                    )
                }
            }

            // All Contacts Section
            item {
                Text(
                    text = "جميع جهات الاتصال",
                    color = SamsungTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp, top = 12.dp, bottom = 2.dp)
                )
            }

            items(contacts, key = { it.id }) { contact ->
                SamsungContactCard(
                    contact = contact,
                    onCall = { viewModel.makeRealCall(context, contact.phoneNumber, contact.name) },
                    onMessage = { viewModel.sendSms(context, contact.phoneNumber) },
                    onToggleFavorite = { viewModel.toggleFavorite(contact.id) },
                    onDelete = { viewModel.deleteContact(contact.id) }
                )
            }
        }
    }

    // Add Contact Dialog
    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var newExt by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة جهة اتصال جديدة", color = SamsungTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("الاسم") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SamsungTextPrimary,
                            unfocusedTextColor = SamsungTextPrimary,
                            focusedBorderColor = SamsungGreen
                        )
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("رقم الهاتف") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SamsungTextPrimary,
                            unfocusedTextColor = SamsungTextPrimary,
                            focusedBorderColor = SamsungGreen
                        )
                    )
                    OutlinedTextField(
                        value = newExt,
                        onValueChange = { newExt = it },
                        label = { Text("التحويلة (اختياري)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SamsungTextPrimary,
                            unfocusedTextColor = SamsungTextPrimary,
                            focusedBorderColor = SamsungGreen
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newPhone.isNotBlank()) {
                            viewModel.addContact(newName, newPhone, newExt.ifBlank { null })
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SamsungGreen)
                ) {
                    Text("حفظ", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = SamsungTextSecondary)
                }
            },
            containerColor = SamsungSurfaceVariant,
            shape = RoundedCornerShape(22.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SamsungContactCard(
    contact: Contact,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SamsungSurface)
            .combinedClickable(
                onClick = onCall,
                onLongClick = { showDeleteConfirm = true }
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("contact_item_${contact.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SamsungSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1).uppercase(),
                    color = SamsungGreen,
                    fontSize = 17.sp,
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
                    text = "${contact.phoneNumber}${if (!contact.extension.isNullOrBlank()) " • تحويلة ${contact.extension}" else ""}",
                    color = SamsungTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "المفضلة",
                    tint = if (contact.isFavorite) Color(0xFFFFB300) else SamsungTextMuted,
                    modifier = Modifier.size(19.dp)
                )
            }

            IconButton(
                onClick = onMessage,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SamsungBlue.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "رسالة",
                    tint = SamsungBlue,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onCall,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SamsungGreen.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "اتصال",
                    tint = SamsungGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف جهة الاتصال؟", color = SamsungTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("هل تريد بالتأكيد حذف جهة الاتصال ${contact.name}؟", color = SamsungTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SamsungRed)
                ) {
                    Text("حذف", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء", color = SamsungTextSecondary)
                }
            },
            containerColor = SamsungSurfaceVariant,
            shape = RoundedCornerShape(22.dp)
        )
    }
}
