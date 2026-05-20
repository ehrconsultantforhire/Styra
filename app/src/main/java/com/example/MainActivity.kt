package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.StyraApp
import com.example.ui.theme.StyraTheme
import com.example.ui.FashionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StyraTheme {
                val viewModel: FashionViewModel = viewModel()
                StyraApp(viewModel = viewModel)
            }
        }
    }
}
