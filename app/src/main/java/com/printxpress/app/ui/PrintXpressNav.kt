package com.printxpress.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.printxpress.app.data.PrintXpressRepository
import com.printxpress.app.data.model.ProductCategory
import com.printxpress.app.data.model.UserEntity
import com.printxpress.app.ui.navigation.Routes
import com.printxpress.app.ui.screens.AddressesScreen
import com.printxpress.app.ui.screens.CategoryScreen
import com.printxpress.app.ui.screens.DesignsScreen
import com.printxpress.app.ui.screens.FaqScreen
import com.printxpress.app.ui.screens.GuidelinesScreen
import com.printxpress.app.ui.screens.HomeScreen
import com.printxpress.app.ui.screens.LoginScreen
import com.printxpress.app.ui.screens.NotificationsScreen
import com.printxpress.app.ui.screens.OrderDetailScreen
import com.printxpress.app.ui.screens.OrdersScreen
import com.printxpress.app.ui.screens.PlaceOrderScreen
import com.printxpress.app.ui.screens.ProductDetailScreen
import com.printxpress.app.ui.screens.ProfileScreen
import com.printxpress.app.ui.screens.RegisterScreen

@Composable
fun PrintXpressNav(repository: PrintXpressRepository) {
    val sessionUser by repository.observeSessionUser().collectAsState(initial = null)
    var bootstrapped by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<UserEntity?>(null) }

    // Keep a local mirror so profile edits refresh the UI without waiting on Flow.
    if (!bootstrapped) {
        bootstrapped = true
    }
    if (sessionUser != null && currentUser?.id != sessionUser?.id) {
        currentUser = sessionUser
    }
    if (sessionUser == null && currentUser != null) {
        currentUser = null
    }

    val user = currentUser ?: sessionUser
    val navController = rememberNavController()
    val start = if (user == null) Routes.LOGIN else Routes.HOME

    // Remount nav when auth gate changes
    androidx.compose.runtime.key(user?.id ?: -1L) {
        NavHost(
            navController = navController,
            startDestination = start
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    repository = repository,
                    onLoggedIn = {
                        // Session flow updates; key remount handles destination
                    },
                    onGoRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    repository = repository,
                    onRegistered = { },
                    onGoLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.HOME) {
                val active = user
                if (active == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    HomeScreen(
                        repository = repository,
                        user = active,
                        onCategory = { navController.navigate(Routes.category(it.name)) },
                        onProduct = { navController.navigate(Routes.product(it)) },
                        onOrders = { navController.navigate(Routes.ORDERS) },
                        onProfile = { navController.navigate(Routes.PROFILE) },
                        onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                        onGuidelines = { navController.navigate(Routes.GUIDELINES) },
                        onFaq = { navController.navigate(Routes.FAQ) }
                    )
                }
            }
            composable(
                Routes.CATEGORY,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { entry ->
                val category = ProductCategory.valueOf(entry.arguments!!.getString("category")!!)
                CategoryScreen(
                    repository = repository,
                    category = category,
                    onBack = { navController.popBackStack() },
                    onProduct = { navController.navigate(Routes.product(it)) }
                )
            }
            composable(
                Routes.PRODUCT,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { entry ->
                val productId = entry.arguments!!.getString("productId")!!
                ProductDetailScreen(
                    repository = repository,
                    productId = productId,
                    onBack = { navController.popBackStack() },
                    onOrder = { navController.navigate(Routes.placeOrder(it)) }
                )
            }
            composable(
                Routes.PLACE_ORDER,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { entry ->
                val active = user ?: return@composable
                val productId = entry.arguments!!.getString("productId")!!
                PlaceOrderScreen(
                    repository = repository,
                    user = active,
                    productId = productId,
                    onBack = { navController.popBackStack() },
                    onPlaced = { orderId ->
                        navController.navigate(Routes.order(orderId)) {
                            popUpTo(Routes.HOME)
                        }
                    }
                )
            }
            composable(Routes.ORDERS) {
                val active = user ?: return@composable
                OrdersScreen(
                    repository = repository,
                    user = active,
                    onBack = { navController.popBackStack() },
                    onOrder = { navController.navigate(Routes.order(it)) }
                )
            }
            composable(
                Routes.ORDER_DETAIL,
                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
            ) { entry ->
                OrderDetailScreen(
                    repository = repository,
                    orderId = entry.arguments!!.getLong("orderId"),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PROFILE) {
                val active = user ?: return@composable
                ProfileScreen(
                    repository = repository,
                    user = active,
                    onBack = { navController.popBackStack() },
                    onAddresses = { navController.navigate(Routes.ADDRESSES) },
                    onDesigns = { navController.navigate(Routes.DESIGNS) },
                    onOrders = { navController.navigate(Routes.ORDERS) },
                    onLogout = { currentUser = null },
                    onUserUpdated = { currentUser = it }
                )
            }
            composable(Routes.ADDRESSES) {
                val active = user ?: return@composable
                AddressesScreen(
                    repository = repository,
                    user = active,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.DESIGNS) {
                val active = user ?: return@composable
                DesignsScreen(
                    repository = repository,
                    user = active,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.NOTIFICATIONS) {
                val active = user ?: return@composable
                NotificationsScreen(
                    repository = repository,
                    user = active,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.GUIDELINES) {
                GuidelinesScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.FAQ) {
                FaqScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
