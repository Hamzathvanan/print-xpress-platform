package com.printxpress.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.printxpress.app.data.PrintXpressRepository
import com.printxpress.app.data.model.AddressEntity
import com.printxpress.app.data.model.SavedDesignEntity
import com.printxpress.app.data.model.UserEntity
import com.printxpress.app.ui.components.EmptyState
import com.printxpress.app.ui.components.ErrorBanner
import com.printxpress.app.ui.components.InfoCard
import com.printxpress.app.ui.components.PrimaryButton
import com.printxpress.app.ui.components.SectionTitle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: PrintXpressRepository,
    user: UserEntity,
    onBack: () -> Unit,
    onAddresses: () -> Unit,
    onDesigns: () -> Unit,
    onOrders: () -> Unit,
    onLogout: () -> Unit,
    onUserUpdated: (UserEntity) -> Unit
) {
    var fullName by remember(user) { mutableStateOf(user.fullName) }
    var email by remember(user) { mutableStateOf(user.email.orEmpty()) }
    var phone by remember(user) { mutableStateOf(user.phone.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    Text(
                        "Back",
                        modifier = Modifier.padding(16.dp).clickable(onClick = onBack),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ErrorBanner(error)
            if (saved) Text("Profile saved", color = MaterialTheme.colorScheme.secondary)
            PrimaryButton(
                text = "Save profile",
                onClick = {
                    scope.launch {
                        val updated = user.copy(
                            fullName = fullName,
                            email = email.ifBlank { null },
                            phone = phone.ifBlank { null }
                        )
                        val result = repository.updateProfile(updated)
                        result.onSuccess {
                            saved = true
                            error = null
                            onUserUpdated(updated)
                        }.onFailure {
                            saved = false
                            error = it.message
                        }
                    }
                }
            )
            SectionTitle("Account")
            InfoCard("Delivery addresses", "Manage pickup-alternative home addresses", onClick = onAddresses)
            InfoCard("Saved designs", "Keep artwork references for reorders", onClick = onDesigns)
            InfoCard("Order history", "View and track every PrintXpress job", onClick = onOrders)
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(text = "Sign out", onClick = {
                scope.launch {
                    repository.logout()
                    onLogout()
                }
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesScreen(
    repository: PrintXpressRepository,
    user: UserEntity,
    onBack: () -> Unit
) {
    val addresses by repository.observeAddresses(user.id).collectAsState(initial = emptyList())
    var label by remember { mutableStateOf("Home") }
    var line1 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Addresses") },
                navigationIcon = {
                    Text(
                        "Back",
                        modifier = Modifier.padding(16.dp).clickable(onClick = onBack),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { SectionTitle("Saved addresses") }
            if (addresses.isEmpty()) {
                item { EmptyState("No addresses yet.") }
            }
            items(addresses) { address ->
                InfoCard(
                    title = address.label + if (address.isDefault) " (default)" else "",
                    body = "${address.line1}, ${address.city}, ${address.district} ${address.postalCode}",
                    onClick = {
                        scope.launch { repository.deleteAddress(address.id) }
                    }
                )
                Text(
                    "Tap card to delete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { SectionTitle("Add address") }
            item {
                OutlinedTextField(label, { label = it }, label = { Text("Label") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(line1, { line1 = it }, label = { Text("Street / line 1") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(city, { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(district, { district = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(postalCode, { postalCode = it }, label = { Text("Postal code") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                FilterChip(
                    selected = isDefault,
                    onClick = { isDefault = !isDefault },
                    label = { Text(if (isDefault) "Default address" else "Set as default") }
                )
            }
            item { ErrorBanner(error) }
            item {
                PrimaryButton(
                    text = "Save address",
                    onClick = {
                        scope.launch {
                            val result = repository.saveAddress(
                                AddressEntity(
                                    userId = user.id,
                                    label = label,
                                    line1 = line1,
                                    city = city,
                                    district = district,
                                    postalCode = postalCode,
                                    isDefault = isDefault
                                )
                            )
                            result.onSuccess {
                                error = null
                                line1 = ""
                                city = ""
                                district = ""
                                postalCode = ""
                            }.onFailure { error = it.message }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignsScreen(
    repository: PrintXpressRepository,
    user: UserEntity,
    onBack: () -> Unit
) {
    val designs by repository.observeDesigns(user.id).collectAsState(initial = emptyList())
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var artworkUri by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        artworkUri = uri?.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved designs") },
                navigationIcon = {
                    Text(
                        "Back",
                        modifier = Modifier.padding(16.dp).clickable(onClick = onBack),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (designs.isEmpty()) {
                item { EmptyState("No saved designs yet.") }
            }
            items(designs) { design ->
                InfoCard(
                    title = design.title,
                    body = design.notes.ifBlank { design.artworkUri ?: "No file attached" },
                    onClick = { scope.launch { repository.deleteDesign(design.id) } }
                )
            }
            item { SectionTitle("Save a design") }
            item {
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
            item {
                PrimaryButton(
                    text = if (artworkUri == null) "Attach artwork" else "Change artwork",
                    onClick = { picker.launch("image/*") }
                )
            }
            item { ErrorBanner(error) }
            item {
                PrimaryButton(
                    text = "Save design",
                    onClick = {
                        scope.launch {
                            val result = repository.saveDesign(
                                SavedDesignEntity(
                                    userId = user.id,
                                    title = title,
                                    notes = notes,
                                    artworkUri = artworkUri
                                )
                            )
                            result.onSuccess {
                                error = null
                                title = ""
                                notes = ""
                                artworkUri = null
                            }.onFailure { error = it.message }
                        }
                    }
                )
            }
        }
    }
}
