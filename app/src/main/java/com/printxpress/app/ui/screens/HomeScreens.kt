package com.printxpress.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.printxpress.app.data.PrintXpressRepository
import com.printxpress.app.data.ProductCatalog
import com.printxpress.app.data.model.ProductCategory
import com.printxpress.app.data.model.UserEntity
import com.printxpress.app.ui.components.InfoCard
import com.printxpress.app.ui.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: PrintXpressRepository,
    user: UserEntity,
    onCategory: (ProductCategory) -> Unit,
    onProduct: (String) -> Unit,
    onOrders: () -> Unit,
    onProfile: () -> Unit,
    onNotifications: () -> Unit,
    onGuidelines: () -> Unit,
    onFaq: () -> Unit
) {
    val products by repository.observeProducts().collectAsState(initial = emptyList())
    val unread by repository.observeUnreadCount(user.id).collectAsState(initial = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PrintXpress", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Hi, ${user.fullName.split(" ").first()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNotifications) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = onOrders) {
                        Icon(Icons.Outlined.ReceiptLong, contentDescription = "Orders")
                    }
                    IconButton(onClick = onProfile) {
                        Icon(Icons.Outlined.Person, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Browse print products by type, material, and size — then upload artwork or instructions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { SectionTitle("Categories") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ProductCategory.entries) { category ->
                        AssistChip(
                            onClick = { onCategory(category) },
                            label = { Text(category.label) }
                        )
                    }
                }
            }
            item { SectionTitle("Seasonal promotions") }
            items(ProductCatalog.seasonalOffers) { offer ->
                InfoCard(title = "Offer", body = offer)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = onGuidelines,
                        label = { Text("Print guidelines") },
                        leadingIcon = { Icon(Icons.Outlined.MenuBook, null) }
                    )
                    AssistChip(
                        onClick = onFaq,
                        label = { Text("FAQ & support") },
                        leadingIcon = { Icon(Icons.Outlined.HelpOutline, null) }
                    )
                }
            }
            if (unread > 0) {
                item {
                    Text(
                        text = "You have $unread unread notification(s).",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.clickable(onClick = onNotifications)
                    )
                }
            }
            item { SectionTitle("Sample gallery") }
            items(products) { product ->
                InfoCard(
                    title = product.name,
                    body = "${ProductCategory.valueOf(product.category).label} · ${product.samplePreview}",
                    trailing = repository.priceLabel(product.basePriceLkr),
                    onClick = { onProduct(product.id) }
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    repository: PrintXpressRepository,
    category: ProductCategory,
    onBack: () -> Unit,
    onProduct: (String) -> Unit
) {
    val products by repository.observeProductsByCategory(category.name)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category.label) },
                navigationIcon = {
                    Text(
                        text = "Back",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable(onClick = onBack),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                InfoCard(
                    title = product.name,
                    body = product.description,
                    trailing = repository.priceLabel(product.basePriceLkr),
                    onClick = { onProduct(product.id) }
                )
            }
        }
    }
}
