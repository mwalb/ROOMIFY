package org.com.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun getPlatformContext(): Any

expect fun openUrl(url: String, context: Any?)
