package org.com.ui.components

import androidx.compose.runtime.Composable
import kotlinx.browser.window

@Composable
actual fun getPlatformContext(): Any = Any()

actual fun openUrl(url: String, context: Any?) {
    window.open(url, "_blank")
}
