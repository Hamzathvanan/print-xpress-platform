package com.printxpress.app.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CATEGORY = "category/{category}"
    const val PRODUCT = "product/{productId}"
    const val PLACE_ORDER = "place_order/{productId}"
    const val ORDERS = "orders"
    const val ORDER_DETAIL = "order/{orderId}"
    const val PROFILE = "profile"
    const val ADDRESSES = "addresses"
    const val DESIGNS = "designs"
    const val NOTIFICATIONS = "notifications"
    const val GUIDELINES = "guidelines"
    const val FAQ = "faq"

    fun category(category: String) = "category/$category"
    fun product(productId: String) = "product/$productId"
    fun placeOrder(productId: String) = "place_order/$productId"
    fun order(orderId: Long) = "order/$orderId"
}
