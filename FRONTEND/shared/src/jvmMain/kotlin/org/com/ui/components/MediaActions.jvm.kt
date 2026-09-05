package org.com.ui.components

import androidx.compose.runtime.Composable
import java.awt.Desktop
import java.net.URI

@Composable
actual fun getPlatformContext(): Any = Any()

actual fun openUrl(url: String, context: Any?) {
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(URI(url))
    }
}
