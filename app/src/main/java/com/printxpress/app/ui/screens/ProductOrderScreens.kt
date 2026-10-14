package com.printxpress.app.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.printxpress.app.data.PrintXpressRepository
import com.printxpress.app.data.model.DeliveryMode
import com.printxpress.app.data.model.ProductCategory
import com.printxpress.app.data.model.ProductEntity
import com.printxpress.app.data.model.UserEntity
import com.printxpress.app.ui.components.ErrorBanner
import com.printxpress.app.ui.components.InfoCard
import com.printxpress.app.ui.components.PrimaryButton
import com.printxpress.app.ui.components.SectionTitle
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    repository: PrintXpressRepository,
    productId: String,
    onBack: () -> Unit,
    onOrder: (String) -> Unit
) {
    var product by remember { mutableStateOf<ProductEntity?>(null) }

    LaunchedEffect(productId) {
        product = repository.getProduct(productId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.name ?: "Product") },
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
        val item = product
        if (item == null) {
            Text("Loading…", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                ProductCategory.valueOf(item.category).label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(item.description, style = MaterialTheme.typography.bodyLarge)
            InfoCard(
                title = "From ${repository.priceLabel(item.basePriceLkr)}",
                body = "Base unit price before quantity. Final total is calculated at checkout."
            )
            InfoCard(title = "Materials", body = item.materials.replace(",", " · "))
            InfoCard(title = "Sizes", body = item.sizes.replace(",", " · "))
            InfoCard(title = "Sample preview", body = item.samplePreview)
            PrimaryButton(text = "Customise & order", onClick = { onOrder(item.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceOrderScreen(
    repository: PrintXpressRepository,
    user: UserEntity,
    productId: String,
    onBack: () -> Unit,
    onPlaced: (Long) -> Unit
) {
    var product by remember { mutableStateOf<ProductEntity?>(null) }
    val addresses by repository.observeAddresses(user.id).collectAsState(initial = emptyList())
    var quantity by remember { mutableStateOf("100") }
    var paperType by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var customText by remember { mutableStateOf("") }
    var artworkUri by remember { mutableStateOf<String?>(null) }
    var deliveryMode by remember { mutableStateOf(DeliveryMode.PICKUP) }
    var addressId by remember { mutableStateOf<Long?>(null) }
    var scheduledDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        artworkUri = uri?.toString()
    }

    LaunchedEffect(productId) {
        product = repository.getProduct(productId)
        product?.let {
            paperType = it.materials.split(",").first()
            size = it.sizes.split(",").first()
        }
    }

    LaunchedEffect(addresses) {
        if (addressId == null) {
            addressId = addresses.firstOrNull { it.isDefault }?.id ?: addresses.firstOrNull()?.id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Place order") },
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
        val item = product ?: return@Scaffold
        val materials = item.materials.split(",")
        val sizes = item.sizes.split(",")
        val qty = quantity.toIntOrNull() ?: 0
        val total = item.basePriceLkr * qty

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(item.name, style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter { ch -> ch.isDigit() }.take(5) },
                label = { Text("Quantity") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            DropdownField("Material / paper", paperType, materials) { paperType = it }
            DropdownField("Size", size, sizes) { size = it }
            OutlinedTextField(
                value = customText,
                onValueChange = { customText = it },
                label = { Text("Custom text / print instructions") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )
            PrimaryButton(
                text = if (artworkUri == null) "Upload artwork" else "Change artwork",
                onClick = { picker.launch("image/*") }
            )
            if (artworkUri != null) {
                Text("Selected: $artworkUri", style = MaterialTheme.typography.bodySmall)
            }
            SectionTitle("Pickup or delivery")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = deliveryMode == DeliveryMode.PICKUP,
                    onClick = { deliveryMode = DeliveryMode.PICKUP },
                    label = { Text("Pickup") }
                )
                FilterChip(
                    selected = deliveryMode == DeliveryMode.DELIVERY,
                    onClick = { deliveryMode = DeliveryMode.DELIVERY },
                    label = { Text("Delivery") }
                )
            }
            if (deliveryMode == DeliveryMode.DELIVERY) {
                if (addresses.isEmpty()) {
                    Text(
                        "Add a delivery address in Profile before ordering for delivery.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    DropdownField(
                        label = "Delivery address",
                        selected = addresses.firstOrNull { it.id == addressId }?.let {
                            "${it.label} — ${it.line1}, ${it.city}"
                        } ?: "",
                        options = addresses.map { "${it.id}|${it.label} — ${it.line1}, ${it.city}" }
                    ) { chosen ->
                        addressId = chosen.substringBefore("|").toLongOrNull()
                    }
                }
            }
            OutlinedTextField(
                value = scheduledDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Scheduled date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                scheduledDate = "%04d-%02d-%02d".format(y, m + 1, d)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                shape = RoundedCornerShape(12.dp)
            )
            // Make the field open picker reliably
            PrimaryButton(
                text = if (scheduledDate.isBlank()) "Choose date" else "Date: $scheduledDate",
                onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            scheduledDate = "%04d-%02d-%02d".format(y, m + 1, d)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Order notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            InfoCard(
                title = "Estimated total",
                body = repository.priceLabel(total.coerceAtLeast(0.0))
            )
            ErrorBanner(error)
            PrimaryButton(
                text = "Confirm order",
                loading = loading,
                onClick = {
                    loading = true
                    error = null
                    scope.launch {
                        val result = repository.placeOrder(
                            userId = user.id,
                            product = item,
                            quantity = qty,
                            paperType = paperType,
                            size = size,
                            customText = customText,
                            artworkUri = artworkUri,
                            deliveryMode = deliveryMode,
                            addressId = if (deliveryMode == DeliveryMode.DELIVERY) addressId else null,
                            scheduledDate = scheduledDate,
                            notes = notes
                        )
                        loading = false
                        result.onSuccess { onPlaced(it) }
                            .onFailure { error = it.message }
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.substringAfter("|", selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.substringAfter("|", option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
