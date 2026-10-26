package com.gym.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.gym.core.ui.theme.GymTheme
import com.gym.tracker.navigation.GymNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymTheme {
                val navController = rememberNavController()
                GymNavHost(navController = navController)
            }
        }
    }
}
