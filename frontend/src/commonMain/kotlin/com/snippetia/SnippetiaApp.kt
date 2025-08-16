package com.snippetia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.snippetia.presentation.screen.SplashScreen
import com.snippetia.presentation.theme.SnippetiaTheme
import com.snippetia.di.initKoin
import org.koin.compose.KoinApplication

@Composable
fun SnippetiaApp() {
    KoinApplication(application = {
        initKoin()
    }) {
        SnippetiaTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Navigator(SplashScreen()) { navigator ->
                    SlideTransition(navigator)
                }
            }
        }
    }
}