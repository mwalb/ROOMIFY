package org.com.ui.components

import androidx.compose.runtime.Composable
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun getPlatformContext(): Any = Any()

actual fun openUrl(url: String, context: Any?) {
    val nsUrl = NSURL.URLWithString(url)
    if (nsUrl != null) {
        UIApplication.sharedApplication.openURL(nsUrl)
    }
}
