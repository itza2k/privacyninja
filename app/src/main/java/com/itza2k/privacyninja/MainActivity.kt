package com.itza2k.privacyninja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.itza2k.privacyninja.repository.PrivacyRepository
import com.itza2k.privacyninja.ui.navigation.AppNavigation
import com.itza2k.privacyninja.ui.theme.PrivacyNinjaTheme


class MainActivity : ComponentActivity() {

    private lateinit var privacyRepository: PrivacyRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize repository
        privacyRepository = PrivacyRepository(applicationContext)

        enableEdgeToEdge()
        setContent {
            PrivacyNinjaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(privacyRepository)
                }
            }
        }
    }
}
