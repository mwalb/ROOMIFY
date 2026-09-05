package org.com

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    println("Roomify: Kotlin/Wasm main() started")

    val composeTarget =
        document.getElementById("ComposeTarget")
            ?: error(
                "Roomify: ComposeTarget not found"
            )

    println(
        "Roomify: Mounting Compose application..."
    )

    ComposeViewport(
        content = {
            App()
        }
    )

    println(
        "Roomify: Compose application mounted"
    )
}