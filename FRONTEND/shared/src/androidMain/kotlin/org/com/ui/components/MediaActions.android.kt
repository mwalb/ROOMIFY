package org.com.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getPlatformContext(): Any = LocalContext.current

actual fun openUrl(url: String, context: Any?) {
    val androidContext = context as? Context
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    androidContext?.startActivity(intent)
}
