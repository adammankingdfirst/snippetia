package com.codeshare

import androidx.compose.ui.window.ComposeUIViewController
import com.codeshare.ui.CodeShareApp
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    CodeShareApp()
}