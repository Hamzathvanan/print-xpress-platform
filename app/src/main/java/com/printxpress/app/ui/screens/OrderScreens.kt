package com.printxpress.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.printxpress.app.data.PrintXpressRepository
import com.printxpress.app.data.model.DeliveryMode
import com.printxpress.app.data.model.OrderStatus
import com.printxpress.app.data.model.UserEntity
import com.printxpress.app.ui.components.EmptyState
import com.printxpress.app.ui.components.ErrorBanner
import com.printxpress.app.ui.components.InfoCard
import com.printxpress.app.ui.components.PrimaryButton
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    repository: PrintXpressRepository,
    user: UserEntity,
    onBack: () -> Unit,
    onOrder: (Long) -> Unit
) {
    val orders by repository.observeOrders(user.id).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My orders") },
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
        if (orders.isEmpty()) {
            EmptyState("No orders yet. Browse products to place your first print job.")
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                val status = OrderStatus.valueOf(order.status)
                InfoCard(
                    title = "Order #${order.id}",
                    body = "${status.label} · ${order.scheduledDate} · ${DeliveryMode.valueOf(order.deliveryMode).label}",
                    trailing = repository.priceLabel(order.totalPriceLkr),
                    onClick = { onOrder(order.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    repository: PrintXpressRepository,
    orderId: Long,
    onBack: () -> Unit
) {
    val orderFlow = repository.observeOrder(orderId)
    val order by orderFlow.collectAsState(initial = null)
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order #$orderId") },
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
        val current = order
        if (current == null) {
            Text("Loading…", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        val status = OrderStatus.valueOf(current.status)
        val canEdit = status == OrderStatus.RECEIVED || status == OrderStatus.PROCESSING

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(title = "Status", body = status.label)
            InfoCard(
                title = "Specs",
                body = "Qty ${current.quantity} · ${current.size} · ${current.paperType}"
            )
            InfoCard(
                title = DeliveryMode.valueOf(current.deliveryMode).label,
                body = "Scheduled: ${current.scheduledDate}"
            )
            if (!current.customText.isNullOrBlank()) {
                InfoCard(title = "Instructions", body = current.customText)
            }
            if (!current.artworkUri.isNullOrBlank()) {
                InfoCard(title = "Artwork", body = current.artworkUri)
            }
            InfoCard(
                title = "Total",
                body = repository.priceLabel(current.totalPriceLkr)
            )
            ErrorBanner(error)
            if (message != null) {
                Text(message!!, color = MaterialTheme.colorScheme.secondary)
            }
            if (canEdit) {
                PrimaryButton(
                    text = "Reschedule",
                    onClick = {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val date = "%04d-%02d-%02d".format(y, m + 1, d)
                                scope.launch {
                                    val result = repository.rescheduleOrder(current, date)
                                    result.onSuccess {
                                        message = "Rescheduled to $date"
                                        error = null
                                    }.onFailure { error = it.message }
                                }
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                )
                PrimaryButton(
                    text = "Cancel order",
                    onClick = {
                        scope.launch {
                            val result = repository.cancelOrder(current)
                            result.onSuccess {
                                message = "Order cancelled"
                                error = null
                            }.onFailure { error = it.message }
                        }
                    }
                )
            } else if (status != OrderStatus.CANCELLED && status != OrderStatus.COMPLETED) {
                Text(
                    "Cancellation and rescheduling are locked once printing begins.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (status != OrderStatus.CANCELLED && status != OrderStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryButton(
                    text = "Demo: advance status",
                    onClick = {
                        scope.launch {
                            val result = repository.advanceOrderDemo(current)
                            result.onSuccess {
                                message = "Status advanced"
                                error = null
                            }.onFailure { error = it.message }
                        }
                    }
                )
                Text(
                    "Demo control to simulate shop workflow for testing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
