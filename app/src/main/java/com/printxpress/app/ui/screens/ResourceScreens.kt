package com.printxpress.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.printxpress.app.data.PrintXpressRepository
import com.printxpress.app.data.model.UserEntity
import com.printxpress.app.ui.components.EmptyState
import com.printxpress.app.ui.components.InfoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    repository: PrintXpressRepository,
    user: UserEntity,
    onBack: () -> Unit
) {
    val notifications by repository.observeNotifications(user.id).collectAsState(initial = emptyList())

    LaunchedEffect(user.id) {
        repository.markNotificationsRead(user.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications & offers") },
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
        if (notifications.isEmpty()) {
            EmptyState("No notifications yet.")
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(notifications) { item ->
                InfoCard(
                    title = item.title,
                    body = item.body,
                    trailing = item.type.uppercase()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidelinesScreen(onBack: () -> Unit) {
    val guidelines = listOf(
        "File requirements" to "Upload PDF, PNG, or JPG. Prefer PDF for multi-page jobs. Keep fonts outlined.",
        "Colour formats" to "Use CMYK for professional print accuracy. RGB designs may shift when converted.",
        "Bleed margins" to "Include 3mm bleed on all sides and keep critical text 5mm inside the trim.",
        "Resolution" to "Raster artwork should be 300 DPI at final print size.",
        "Safe zone" to "Avoid placing logos or phone numbers near edges or fold lines."
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Print guidelines") },
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
            items(guidelines) { (title, body) ->
                InfoCard(title = title, body = body)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(onBack: () -> Unit) {
    val faqs = listOf(
        "How long does printing take?" to "Most standard jobs are ready within 24–48 hours after artwork approval.",
        "Can I cancel an order?" to "Yes — cancel or reschedule while status is Received or Processing, before printing begins.",
        "Do you deliver island-wide?" to "Pickup is available at partner centres; home delivery covers major Sri Lankan cities.",
        "What if my file is wrong?" to "Contact support from this screen’s guidance or note corrections in order instructions before printing starts.",
        "Who do I contact?" to "Email support@printxpress.lk or call +94 11 200 3000 (coursework demo contacts)."
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FAQ & support") },
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
            items(faqs) { (q, a) ->
                InfoCard(title = q, body = a)
            }
        }
    }
}
