package com.printxpress.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.printxpress.app.ui.PrintXpressNav
import com.printxpress.app.ui.theme.PrintXpressTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PrintXpressApp
        setContent {
            PrintXpressTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PrintXpressNav(repository = app.repository)
                }
            }
        }
    }
}
