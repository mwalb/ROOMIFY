/**
 * Roomify Web Application
 *
 * Kotlin/Wasm bootstrap.
 */

(function () {

    "use strict";

    console.log(
        "Roomify: webApp.js loading..."
    );

    let started = false;

    function initApp() {

        if (started) {
            return true;
        }

        console.log(
            "Roomify: Initializing application..."
        );

        let composeTarget =
            document.getElementById(
                "ComposeTarget"
            );

        if (!composeTarget) {

            console.log(
                "Roomify: Creating ComposeTarget..."
            );

            composeTarget =
                document.createElement("div");

            composeTarget.id =
                "ComposeTarget";

            composeTarget.setAttribute(
                "style",
                `
                position:fixed;
                top:0;
                left:0;
                width:100vw;
                height:100vh;
                z-index:10;
                pointer-events:none;
                background:transparent;
                `
            );

            document.body.appendChild(
                composeTarget
            );
        }

        /*
         * Kotlin/Wasm generated module.
         */

        if (
            typeof window.kotlin ===
            "undefined"
        ) {

            console.log(
                "Roomify: Kotlin/Wasm module not ready..."
            );

            return false;
        }

        /*
         * Your current project exposes main().
         */

        if (
            typeof window.main ===
            "function"
        ) {

            console.log(
                "Roomify: Starting app via main()"
            );

            started = true;

            window.main();

            console.log(
                "Roomify: App started successfully"
            );

            return true;
        }

        console.warn(
            "Roomify: window.main() not available yet"
        );

        return false;
    }

    function retryInit(attempts) {

        if (attempts > 40) {

            console.error(
                "Roomify: Failed to start Kotlin/Wasm application"
            );

            return;
        }

        if (!initApp()) {

            setTimeout(
                function () {
                    retryInit(
                        attempts + 1
                    );
                },
                250
            );
        }
    }

    /*
     * ============================================================
     * START
     * ============================================================
     */

    if (
        document.readyState ===
        "loading"
    ) {

        document.addEventListener(
            "DOMContentLoaded",
            function () {
                retryInit(0);
            }
        );

    } else {

        retryInit(0);
    }

    /*
     * ============================================================
     * DEBUG
     * ============================================================
     */

    window.RoomifyDebug = {

        status: function () {

            console.log(
                "========== ROOMIFY STATUS =========="
            );

            console.log(
                "ComposeTarget:",
                document.getElementById(
                    "ComposeTarget"
                )
            );

            console.log(
                "Map container:",
                document.getElementById(
                    "google-map-container"
                )
            );

            console.log(
                "Kotlin:",
                typeof window.kotlin
            );

            console.log(
                "main:",
                typeof window.main
            );

            console.log(
                "Google Maps:",
                typeof window.google
            );

            console.log(
                "Roomify map:",
                window.roomifyMap
            );

            console.log(
                "Markers:",
                window.roomifyMarkers
                    ? window.roomifyMarkers.length
                    : 0
            );

            console.log(
                "===================================="
            );
        },

        showMap: function () {

            if (
                window.roomifyMap &&
                window.roomifyResizeMap
            ) {

                window.roomifyResizeMap();

                console.log(
                    "Roomify: map refreshed"
                );

                return true;
            }

            console.warn(
                "Roomify: map is not ready"
            );

            return false;
        }
    };

    console.log(
        "Roomify: webApp.js loaded successfully"
    );

})();