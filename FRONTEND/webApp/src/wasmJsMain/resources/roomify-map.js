(function () {

    "use strict";

    /*
     * ============================================================
     * ROOMIFY GOOGLE MAP STATE
     * ============================================================
     */

    window.roomifyMap = null;
    window.roomifyMarkers = [];
    window.roomifyInfoOverlay = null;
    window.roomifySelectedRoomId = null;
    window.roomifyPopupPinned = false;
    window.roomifyViewedRoomIds = new Set();
    window.roomifySavedRoomIds = new Set();
    window.roomifyMarkerClusterer = null;

    /*
     * Keep the complete room dataset.
     *
     * Search works against this list and then rebuilds the
     * displayed markers.
     */
    window.roomifyAllRooms = [];
    window.roomifyAreaSuggestions = [];

    window.roomifySearchQuery = "";

    function fetchAreaSuggestions() {
        fetch('/api/rooms/areas')
            .then(response => response.json())
            .then(data => {
                window.roomifyAreaSuggestions = data || [];
                console.log("Roomify: Fetched " + window.roomifyAreaSuggestions.length + " area suggestions");
            })
            .catch(err => console.error("Roomify: Failed to fetch area suggestions", err));
    }

    /*
     * ============================================================
     * ROOMIFY COLORS
     * ============================================================
     */

    var ROOMIFY_BLUE_START = "#1A237E";
    var ROOMIFY_BLUE_END = "#3949AB";
    var ROOMIFY_BLUE = "#3949AB";
    var ROOMIFY_SELECTED_ORANGE = "#FF9800";
    var ROOMIFY_WHITE = "#FFFFFF";
    var ROOMIFY_WHITE_90 = "rgba(255,255,255,0.90)";
    var ROOMIFY_WHITE_80 = "rgba(255,255,255,0.80)";
    var ROOMIFY_WHITE_70 = "rgba(255,255,255,0.70)";
    var ROOMIFY_WHITE_55 = "rgba(255,255,255,0.55)";
    var ROOMIFY_WHITE_25 = "rgba(255,255,255,0.25)";
    var ROOMIFY_GREEN = "#2E7D32";
    var ROOMIFY_YELLOW = "#F9A825";
    var ROOMIFY_RED = "#C62828";
    var ROOMIFY_ORANGE = "#FF9800";
    var ROOMIFY_VIEWED_GRAY = "#9E9E9E";
    var ROOMIFY_SAVED_PINK = "#E91E63";


    /*
     * ============================================================
     * MAP STYLES
     * ============================================================
     */

    var MAP_STYLE_DETAILED = [
        { "featureType": "all", "elementType": "labels.text.fill", "stylers": [{ "color": "#616161" }] },
        { "featureType": "poi", "elementType": "labels.icon", "stylers": [{ "saturation": -100 }, { "lightness": 10 }] },
        { "featureType": "poi", "elementType": "labels.text.fill", "stylers": [{ "color": "#616161" }] },
        { "featureType": "road", "elementType": "all", "stylers": [{ "saturation": -50 }] }
    ];


    /*
     * ============================================================
     * SHOW MAP
     * ============================================================
     */

    window.roomifyShowMap = function () {
        console.log("[ROOMIFY MAP] SHOW MAP START");

        var map = document.getElementById("google-map-container");
        var sidebar = document.getElementById("roomify-sidebar");
        var compose = document.getElementById("ComposeTarget");

        if (map) {
            map.classList.add("map-mode");
            map.style.display = "block";
            map.style.visibility = "visible";
            map.style.opacity = "1";
            map.style.zIndex = "9999";
            console.log("[ROOMIFY MAP] map container shown with 'map-mode'");
        } else {
            console.error("[ROOMIFY MAP] map container NOT FOUND");
        }

        if (sidebar) {
            sidebar.style.display = "flex";
            console.log("[ROOMIFY MAP] sidebar element shown");
        } else {
            console.warn("[ROOMIFY MAP] sidebar element NOT FOUND");
        }

        if (compose) {
            compose.style.left = "400px";
            compose.style.width = "calc(100vw - 400px)";
            console.log("[ROOMIFY MAP] ComposeTarget squeezed to right");
        }

        setTimeout(function () {

            if (
                window.roomifyMap &&
                window.google &&
                window.google.maps
            ) {

                google.maps.event.trigger(
                    window.roomifyMap,
                    "resize"
                );
            }

        }, 100);

        setTimeout(function () {

            if (
                window.roomifyMap &&
                window.google &&
                window.google.maps
            ) {

                google.maps.event.trigger(
                    window.roomifyMap,
                    "resize"
                );
            }

        }, 500);
    };

    window.roomifyResizeMap = function () {
        if (
            window.roomifyMap &&
            window.google &&
            window.google.maps
        ) {
            google.maps.event.trigger(window.roomifyMap, "resize");
        }
    };


    /*
     * ============================================================
     * MAP VISIBILITY CONTROL - EXPOSED FOR KOTLIN
     * ============================================================
     */

    window.roomifyHideMap = function() {
        console.log("[ROOMIFY MAP] HIDE MAP");

        var map = document.getElementById("google-map-container");
        var sidebar = document.getElementById("roomify-sidebar");
        var compose = document.getElementById("ComposeTarget");

        if (map) {
            map.classList.remove("map-mode");
            map.style.display = "none";
            map.style.visibility = "hidden";
            map.style.opacity = "0";
            map.style.zIndex = "-1";
            console.log("[ROOMIFY MAP] map hidden");
        }

        if (sidebar) {
            sidebar.style.display = "none";
            console.log("[ROOMIFY MAP] sidebar hidden");
        }

        if (compose) {
            compose.style.left = "0";
            compose.style.width = "100vw";
            console.log("[ROOMIFY MAP] ComposeTarget restored to full width");
        }
    };

    window.roomifyShowMapDirect = function() {
        console.log("[ROOMIFY MAP] SHOW MAP DIRECT");
        window.roomifyShowMap();
    };


    /*
     * ============================================================
     * SEARCH / SIDEBAR STYLES
     * ============================================================
     */

    function injectRoomifyControlStyles() {

        if (
            document.getElementById(
                "roomify-map-control-styles"
            )
        ) {
            return;
        }

        var style =
            document.createElement("style");

        style.id =
            "roomify-map-control-styles";

        style.textContent = `

            .roomify-search-control {
                display: flex;
                align-items: center;
                width: min(520px, calc(100vw - 110px));
                height: 48px;
                margin-top: 14px;
                background: rgba(255, 255, 255, 0.96);
                border-radius: 18px;
                box-shadow: 0 3px 12px rgba(0,0,0,0.15);
                border: 1px solid #BDBDBD;
                position: relative;
            }

            .roomify-autocomplete-dropdown {
                position: absolute;
                top: 100%;
                left: 0;
                right: 0;
                background: white;
                border-radius: 0 0 18px 18px;
                box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                border: 1px solid #BDBDBD;
                border-top: none;
                z-index: 10002;
                max-height: 250px;
                overflow-y: auto;
                display: none;
            }

            .roomify-autocomplete-item {
                padding: 12px 16px;
                cursor: pointer;
                border-bottom: 1px solid #F5F5F5;
                color: #1A237E;
                font-weight: 500;
                display: flex;
                align-items: center;
            }

            .roomify-autocomplete-item:hover {
                background: #F0F2F5;
            }

            .roomify-autocomplete-item:last-child {
                border-bottom: none;
            }

            .roomify-search-icon {
                width: 20px;
                height: 20px;
                margin-left: 15px;
                flex-shrink: 0;
                color: #1A237E;
                opacity: 0.78;
            }

            .roomify-search-input {
                flex: 1;
                min-width: 0;
                height: 100%;
                border: 0;
                outline: 0;
                padding: 0 12px;
                font-family: Arial, sans-serif;
                font-size: 16px;
                color: #1A237E;
                background: transparent;
                font-weight: 600;
            }

            .roomify-search-input::placeholder {
                color: #1A237E;
                opacity: 0.52;
                font-weight: 400;
            }

            .roomify-search-clear {
                width: 38px;
                height: 38px;
                margin-right: 5px;
                border: 0;
                border-radius: 50%;
                background: transparent;
                color: #1A237E;
                opacity: 0.70;
                cursor: pointer;
                font-size: 18px;
                display: none;
                align-items: center;
                justify-content: center;
            }

            .roomify-search-clear:hover {
                background: rgba(26, 35, 126, 0.08);
            }

            .roomify-menu-button {
                width: 46px;
                height: 46px;
                margin-top: 14px;
                margin-left: 12px;
                border: 0;
                border-radius: 13px;
                background: linear-gradient(135deg, #1A237E, #3949AB);
                color: #ffffff;
                box-shadow: 0 3px 12px rgba(0,0,0,0.20);
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 22px;
                font-weight: bold;
            }

            .roomify-menu-button:hover {
                background: linear-gradient(135deg, #1A237E, #3949AB);
                opacity: 0.92;
            }

            .roomify-top-right-controls {
                margin-top: 14px;
                margin-right: 12px;
            }

            .roomify-status-control {
                background: rgba(255, 255, 255, 0.96);
                border-radius: 13px;
                box-shadow: 0 3px 12px rgba(0,0,0,0.15);
                border: 1px solid #BDBDBD;
                height: 46px;
                padding: 0 10px;
                display: flex;
                align-items: center;
                cursor: pointer;
                font-family: Arial, sans-serif;
                font-size: 14px;
                font-weight: bold;
                color: #1A237E;
                position: relative;
                min-width: 110px;
            }

            .roomify-status-dropdown {
                position: absolute;
                top: calc(100% + 8px);
                right: 0;
                background: white;
                border-radius: 13px;
                box-shadow: 0 4px 15px rgba(0,0,0,0.2);
                border: 1px solid #E0E0E0;
                display: none;
                flex-direction: column;
                overflow: hidden;
                z-index: 10001;
                min-width: 140px;
            }

            .roomify-status-dropdown.show {
                display: flex;
            }

            .roomify-status-option {
                padding: 12px 16px;
                cursor: pointer;
                transition: background 0.15s;
                border-bottom: 1px solid #F5F5F5;
                white-space: nowrap;
                color: #444;
            }

            .roomify-status-option:last-child {
                border-bottom: 0;
            }

            .roomify-status-option:hover {
                background: #F5F5F5;
                color: #1A237E;
            }

            .roomify-status-option.active {
                background: rgba(26, 35, 126, 0.08);
                color: #1A237E;
                font-weight: 800;
            }


            /*
             * ====================================================
             * SIDEBAR
             * ====================================================
             */

            /*
             * ====================================================
             * SIDEBAR
             * ====================================================
             */

            .roomify-sidebar {
                position: fixed !important;
                top: 0 !important;
                left: 0 !important;
                width: 400px !important;
                height: 100vh !important;
                background: white !important;
                z-index: 10000 !important;
                box-shadow: 10px 0 30px rgba(0,0,0,0.05);
                display: none;
                flex-direction: column;
                font-family: 'Inter', Arial, sans-serif;
                color: #1A1A1A;
                transform: none !important;
                transition: none !important;
            }

            #google-map-container.map-mode {
                position: fixed !important;
                top: 0 !important;
                left: 400px !important;
                width: calc(100vw - 400px) !important;
                height: 100vh !important;
                margin: 0 !important;
                display: block !important;
                visibility: visible !important;
                opacity: 1 !important;
                z-index: 9999 !important;
            }

            .roomify-sidebar-header {
                padding: 45px 30px;
                background: #3F4494;
                color: white;
                position: relative;
                border-radius: 0 0 45px 0;
            }

            .roomify-sidebar-brand {
                font-size: 32px;
                font-weight: 700;
                margin-bottom: 4px;
            }

            .roomify-sidebar-tagline {
                font-size: 15px;
                opacity: 0.95;
                font-weight: 400;
            }

            .roomify-sidebar-close {
                position: absolute;
                top: 20px;
                right: 20px;
                width: 36px;
                height: 36px;
                border-radius: 50%;
                background: rgba(255, 255, 255, 0.2);
                color: white;
                display: flex !important;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                transition: background 0.2s ease;
            }
            .roomify-sidebar-close:hover {
                background: rgba(255, 255, 255, 0.35);
            }

            .roomify-sidebar-body {
                flex: 1;
                padding: 30px;
                overflow-y: auto;
            }

            .roomify-sidebar-section {
                margin-bottom: 35px;
            }

            .roomify-sidebar-label {
                font-size: 11px;
                font-weight: 700;
                color: #374151;
                text-transform: uppercase;
                letter-spacing: 1.2px;
                margin-bottom: 15px;
            }

            .roomify-sidebar-heading {
                font-size: 22px;
                font-weight: 700;
                color: #000;
                margin-bottom: 20px;
                margin-top: 0;
            }

            .roomify-search-box {
                display: flex;
                align-items: center;
                background: white;
                border: 1px solid #E0E0E0;
                border-radius: 18px;
                padding: 0 18px;
                height: 60px;
                margin-bottom: 15px;
            }

            .roomify-search-box-icon {
                font-size: 20px;
                color: #3F4494;
                margin-right: 15px;
                display: flex;
                align-items: center;
            }

            .roomify-search-box input {
                border: 0;
                background: transparent;
                flex: 1;
                font-size: 16px;
                outline: none;
                color: #333;
                width: 100%;
            }

            .roomify-search-box input::placeholder {
                color: #BDBDBD;
            }

            .roomify-location-btn {
                display: flex;
                align-items: center;
                color: #3F4494;
                font-size: 15px;
                font-weight: 600;
                cursor: pointer;
                padding: 5px 0;
            }

            .roomify-location-icon {
                margin-right: 10px;
                font-size: 18px;
                display: flex;
                align-items: center;
            }

            .roomify-chip-group {
                display: flex;
                flex-wrap: wrap;
                gap: 10px;
            }

            .roomify-chip {
                padding: 10px 18px;
                border: 1px solid #E0E0E0;
                border-radius: 12px;
                font-size: 14px;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.2s;
                background: white;
                display: flex;
                align-items: center;
                color: #333;
            }

            .roomify-chip.active {
                background: #E8EAF6;
                color: #3F4494;
                border-color: #3F4494;
            }

            .roomify-chip .dot {
                width: 9px;
                height: 9px;
                border-radius: 50%;
                margin-right: 10px;
            }

            .roomify-budget-box {
                display: flex;
                align-items: center;
                background: white;
                border: 1px solid #E0E0E0;
                border-radius: 18px;
                padding: 0 18px;
                height: 60px;
            }

            .roomify-budget-icon {
                font-size: 22px;
                color: #3F4494;
                margin-right: 15px;
                display: flex;
                align-items: center;
            }

            .roomify-budget-box input {
                border: 0;
                background: transparent;
                flex: 1;
                font-size: 16px;
                outline: none;
                color: #333;
                width: 100%;
            }

            .roomify-budget-box input::placeholder {
                color: #BDBDBD;
            }

            .roomify-advanced-filters-header {
                display: flex;
                align-items: center;
                padding: 18px 20px;
                background: #F8F9FA;
                border-radius: 18px;
                cursor: pointer;
                margin-bottom: 12px;
                border: 1px solid #F0F2F5;
            }

            .roomify-advanced-filters-icon {
                margin-right: 15px;
                color: #3F4494;
                display: flex;
                align-items: center;
            }

            .roomify-advanced-filters-label {
                flex: 1;
                font-size: 16px;
                font-weight: 700;
                color: #1A1A1A;
            }

            .roomify-advanced-filters-chevron {
                transition: transform 0.3s;
                color: #BDBDBD;
                display: flex;
                align-items: center;
            }

            .roomify-advanced-filters-content {
                display: none;
                padding: 20px;
                background: #F8F9FA;
                border-radius: 0 0 18px 18px;
                margin-top: -30px;
                margin-bottom: 25px;
                border: 1px solid #F0F2F5;
                border-top: 0;
            }

            .roomify-advanced-filters-content.open {
                display: block;
            }

            .dot { width: 9px; height: 9px; border-radius: 50%; margin-right: 10px; }
            .dot.green { background: #4CAF50; }
            .dot.yellow { background: #FFC107; }
            .dot.red { background: #F44336; }

            .roomify-sidebar-footer {
                padding: 25px 30px;
                border-top: 1px solid #F5F5F5;
            }

            .roomify-footer-item {
                display: flex;
                align-items: center;
                cursor: pointer;
                color: #3F4494;
                font-weight: 700;
                font-size: 18px;
            }

            .roomify-footer-icon {
                margin-right: 15px;
                color: #3F4494;
                display: flex;
                align-items: center;
            }


            /*
             * ====================================================
             * SIDEBAR BACKDROP
             * ====================================================
             */

            .roomify-sidebar-backdrop {
                display: none !important;
            }

            @media (max-width: 900px) {
                .roomify-sidebar {
                    position: fixed !important;
                    top: 0 !important;
                    left: -100% !important;
                    width: 320px !important;
                    max-width: 85vw !important;
                    height: 100vh !important;
                    z-index: 99999 !important;
                    transition: left 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
                    box-shadow: 4px 0 24px rgba(0,0,0,0.25) !important;
                }
                .roomify-sidebar.open {
                    left: 0 !important;
                }
                #google-map-container {
                    margin-left: 0 !important;
                    width: 100% !important;
                    height: 100vh !important;
                }
            }


            /*
             * ====================================================
             * SEARCH RESULT COUNT
             * ====================================================
             */

            .roomify-search-count {
                position: fixed;
                top: 73px;
                left: 50%;
                transform: translateX(-50%);
                background: rgba(255,255,255,0.96);
                border-radius: 20px;
                padding: 7px 13px;
                font-family: Arial,sans-serif;
                font-size: 11px;
                font-weight: 700;
                color: #555;
                box-shadow: 0 2px 8px rgba(0,0,0,0.16);
                display: none;
                z-index: 5000;
            }


            /*
             * ====================================================
             * POPUP - ANDROID STYLE
             * ====================================================
             */

            .roomify-popup-container-wrapper {
                cursor: default;
                pointer-events: auto;
            }

            .roomify-popup-container-wrapper * {
                pointer-events: auto;
            }

            .roomify-popup {
                font-family: Arial, sans-serif;
                padding: 16px;
                min-width: 240px;
                max-width: 280px;
                background: linear-gradient(135deg, #1A237E, #3949AB);
                border-radius: 26px;
                box-shadow: 0 8px 30px rgba(0,0,0,0.35);
                border: 1px solid rgba(255,255,255,0.16);
                color: #ffffff;
            }

            .roomify-popup-header {
                display: flex;
                align-items: center;
                margin-bottom: 8px;
            }

            .roomify-popup-icon {
                width: 36px;
                height: 36px;
                border-radius: 15px;
                background: rgba(255,255,255,0.15);
                border: 1px solid rgba(255,255,255,0.16);
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 18px;
                margin-right: 10px;
                flex-shrink: 0;
            }

            .roomify-popup-property-type {
                font-size: 11px;
                font-weight: 600;
                color: rgba(255,255,255,0.80);
                letter-spacing: 0.6px;
            }

            .roomify-popup-image-container {
                width: 100%;
                height: 160px;
                margin: 12px 0;
                border-radius: 20px;
                overflow: hidden;
                background: rgba(255, 255, 255, 0.1);
                border: 1px solid rgba(255, 255, 255, 0.1);
                box-shadow: inset 0 0 20px rgba(0,0,0,0.2);
            }

            .roomify-popup-image {
                width: 100%;
                height: 100%;
                object-fit: cover;
                transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
            }

            .roomify-popup:hover .roomify-popup-image {
                transform: scale(1.05);
            }
                text-transform: uppercase;
            }

            .roomify-popup-title {
                font-size: 20px;
                font-weight: 800;
                color: #ffffff;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }

            .roomify-popup-close {
                width: 32px;
                height: 32px;
                border: 0;
                border-radius: 50%;
                background: rgba(255,255,255,0.13);
                cursor: pointer;
                color: #ffffff;
                font-size: 16px;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-left: auto;
                flex-shrink: 0;
            }

            .roomify-popup-close:hover {
                background: rgba(255,255,255,0.25);
            }

            .roomify-popup-location {
                display: flex;
                align-items: center;
                background: rgba(255,255,255,0.09);
                border-radius: 13px;
                padding: 8px 11px;
                margin: 8px 0 10px 0;
            }

            .roomify-popup-location-icon {
                font-size: 14px;
                margin-right: 7px;
                opacity: 0.8;
            }

            .roomify-popup-location-text {
                font-size: 14px;
                font-weight: 500;
                color: rgba(255, 255, 255, 0.95);
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }

            .roomify-popup-price-row {
                display: flex;
                align-items: center;
                justify-content: space-between;
                margin: 4px 0 8px 0;
            }

            .roomify-popup-price-label {
                font-size: 9px;
                font-weight: 700;
                color: rgba(255,255,255,0.55);
                letter-spacing: 1px;
                text-transform: uppercase;
            }

            .roomify-popup-price {
                font-size: 21px;
                font-weight: 900;
                color: #ffffff;
            }

            .roomify-popup-status {
                display: inline-flex;
                align-items: center;
                padding: 4px 10px;
                border-radius: 50px;
                background: rgba(255,255,255,0.10);
                border: 1px solid rgba(255,255,255,0.17);
                font-size: 11px;
                font-weight: 700;
                color: #ffffff;
            }

            .roomify-popup-status-dot {
                width: 7px;
                height: 7px;
                border-radius: 50%;
                margin-right: 6px;
                flex-shrink: 0;
            }

            .roomify-popup-features {
                display: flex;
                gap: 7px;
                margin: 6px 0 10px 0;
                flex-wrap: wrap;
            }

            .roomify-popup-feature {
                display: flex;
                align-items: center;
                padding: 5px 9px;
                border-radius: 11px;
                background: rgba(255,255,255,0.10);
                border: 1px solid rgba(255,255,255,0.10);
                font-size: 11px;
                font-weight: 500;
                color: rgba(255,255,255,0.90);
            }

            .roomify-popup-feature-icon {
                font-size: 13px;
                margin-right: 4px;
                opacity: 0.7;
            }

            .roomify-popup-button {
                display: block;
                width: 100%;
                padding: 10px 12px;
                border: 0;
                border-radius: 15px;
                background: rgba(255,255,255,0.95);
                color: #1A237E;
                font-family: Arial, sans-serif;
                font-size: 14px;
                font-weight: 700;
                cursor: pointer;
                box-shadow: 0 2px 8px rgba(0,0,0,0.15);
                transition: background 0.2s ease;
                margin-top: 4px;
            }

            .roomify-popup-button:hover {
                background: #ffffff;
            }

            .roomify-popup-button-icon {
                font-size: 15px;
                margin-right: 6px;
            }


            /*
             * ====================================================
             * MOBILE
             * ====================================================
             */

            @media (max-width: 700px) {

                .roomify-search-control {
                    width: calc(100vw - 75px);
                    margin-top: 10px;
                    height: 46px;
                    border-radius: 13px;
                }

                .roomify-menu-button {
                    width: 42px;
                    height: 42px;
                    margin-top: 12px;
                    margin-left: 7px;
                    border-radius: 12px;
                }

                .roomify-search-icon {
                    margin-left: 12px;
                }

                .roomify-search-input {
                    font-size: 13px;
                }

                .roomify-sidebar {
                    width: 290px;
                }

                .roomify-popup {
                    min-width: 200px;
                    max-width: 240px;
                    padding: 14px;
                }

                .roomify-popup-title {
                    font-size: 16px;
                }

                .roomify-popup-price {
                    font-size: 17px;
                }
            }

        `;

        document.head.appendChild(style);
    }


    /*
     * ============================================================
     * SEARCH CONTROL
     * ============================================================
     */

    function createSearchControl() {

        if (
            document.getElementById(
                "roomify-search-control"
            )
        ) {
            return;
        }

        var wrapper =
            document.createElement("div");

        wrapper.id =
            "roomify-search-control";

        wrapper.className =
            "roomify-search-control";

        wrapper.innerHTML = `

            <svg
                class="roomify-search-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="#1A237E"
                stroke-width="2"
                opacity="0.78"
            >
                <circle
                    cx="11"
                    cy="11"
                    r="7"
                ></circle>

                <line
                    x1="16.5"
                    y1="16.5"
                    x2="21"
                    y2="21"
                ></line>
            </svg>

            <input
                id="roomify-search-input"
                class="roomify-search-input"
                type="text"
                autocomplete="off"
                placeholder="Search by area, property name or location"
            />

            <button
                id="roomify-search-clear"
                class="roomify-search-clear"
                type="button"
                aria-label="Clear search"
            >
                ×
            </button>

            <div id="roomify-autocomplete-dropdown" class="roomify-autocomplete-dropdown"></div>
        `;

        var input =
            wrapper.querySelector(
                "#roomify-search-input"
            );

        var clear =
            wrapper.querySelector(
                "#roomify-search-clear"
            );

        var dropdown =
            wrapper.querySelector(
                "#roomify-autocomplete-dropdown"
            );

        fetchAreaSuggestions();

        input.addEventListener(
            "input",
            function () {

                var query =
                    input.value
                        .trim();

                window.roomifySearchQuery =
                    query;

                clear.style.display =
                    query.length > 0
                        ? "flex"
                        : "none";

                updateAutocompleteDropdown(query);
            }
        );

        function updateAutocompleteDropdown(query) {
            if (query.length < 2) {
                dropdown.style.display = "none";
                return;
            }

            var filtered = window.roomifyAreaSuggestions.filter(function(area) {
                return area.toLowerCase().includes(query.toLowerCase()) && area !== query;
            }).slice(0, 5);

            if (filtered.length === 0) {
                dropdown.style.display = "none";
                return;
            }

            dropdown.innerHTML = "";
            filtered.forEach(function(area) {
                var item = document.createElement("div");
                item.className = "roomify-autocomplete-item";
                item.innerHTML = `
                    <span style="margin-right:10px;">📍</span>
                    <span>${area}</span>
                `;
                item.onclick = function() {
                    input.value = area;
                    window.roomifySearchQuery = area;
                    dropdown.style.display = "none";
                    filterRooms(area);
                };
                dropdown.appendChild(item);
            });

            dropdown.style.display = "block";
        }

        // Close dropdown when clicking outside
        document.addEventListener("click", function(e) {
            if (!wrapper.contains(e.target)) {
                dropdown.style.display = "none";
            }
        });


        clear.addEventListener(
            "click",
            function (event) {

                event.preventDefault();
                event.stopPropagation();

                input.value = "";

                window.roomifySearchQuery =
                    "";

                clear.style.display =
                    "none";

                filterRooms("");
            }
        );


        /*
         * Enter key.
         *
         * We do not navigate anywhere.
         * The displayed markers are already filtered.
         */

        input.addEventListener(
            "keydown",
            function (event) {

                if (
                    event.key === "Enter"
                ) {

                    event.preventDefault();

                    filterRooms(
                        input.value.trim()
                    );
                }
            }
        );


        return wrapper;
    }


    /*
     * ============================================================
     * MENU CONTROL
     * ============================================================
     */

    function createMenuControl() {
        return null;
    }


    /*
     * ============================================================
     * STATUS CONTROL
     * ============================================================
     */

    window.roomifyCurrentStatusFilter = "ALL";

    /*
     * ============================================================
     * TOP RIGHT CONTROLS
     * ============================================================
     */

    function createTopRightControls() {
        if (document.getElementById("roomify-top-right-controls")) {
            return;
        }

        var wrapper = document.createElement("div");
        wrapper.id = "roomify-top-right-controls";
        wrapper.className = "roomify-top-right-controls";

        wrapper.appendChild(createStatusControl());

        return wrapper;
    }

    function createStatusControl() {
        var wrapper = document.createElement("div");
        wrapper.id = "roomify-status-control";
        wrapper.className = "roomify-status-control";

        var label = document.createElement("span");
        label.id = "roomify-status-label";
        label.textContent = "ALL";
        label.style.flex = "1";
        wrapper.appendChild(label);

        var arrow = document.createElement("span");
        arrow.innerHTML = `<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="display: flex; align-items: center;"><polyline points="6 9 12 15 18 9"></polyline></svg>`;
        arrow.style.marginLeft = "8px";
        arrow.style.display = "flex";
        arrow.style.alignItems = "center";
        wrapper.appendChild(arrow);

        var dropdown = document.createElement("div");
        dropdown.className = "roomify-status-dropdown";

        var options = ["ALL", "AVAILABLE", "PENDING", "RENTED"];
        options.forEach(function(opt) {
            var option = document.createElement("div");
            option.className = "roomify-status-option";
            if (opt === "ALL") option.classList.add("active");
            option.textContent = opt;
            option.onclick = function(e) {
                e.stopPropagation();
                window.roomifyUpdateFilterStatus(opt);
                dropdown.classList.remove("show");
            };
            dropdown.appendChild(option);
        });

        wrapper.appendChild(dropdown);

        wrapper.onclick = function(e) {
            e.stopPropagation();
            dropdown.classList.toggle("show");
        };

        // Close dropdowns when clicking outside
        document.addEventListener("click", function() {
            var dropdowns = document.querySelectorAll(".roomify-status-dropdown");
            dropdowns.forEach(function(d) { d.classList.remove("show"); });
        });

        return wrapper;
    }

    window.roomifyUpdateFilterStatus = function(status) {
        window.roomifyCurrentStatusFilter = status;

        // Update Label
        var label = document.getElementById("roomify-status-label");
        if (label) label.textContent = status;

        // Update active class in dropdown
        var options = document.querySelectorAll(".roomify-status-option");
        options.forEach(function(opt) {
            if (opt.textContent === status) opt.classList.add("active");
            else opt.classList.remove("active");
        });

        applyMarkerVisibility();
        console.log("Roomify: status filter updated to " + status);
    };

    window.roomifyResetStatusFilter = function() {
        window.roomifyUpdateFilterStatus("ALL");
    };

    function applyMarkerVisibility() {
        if (!Array.isArray(window.roomifyMarkers)) return;

        var filter = window.roomifyCurrentStatusFilter;

        window.roomifyMarkers.forEach(function(entry) {
            if (!entry || !entry.marker || !entry.room) return;

            var status = String(entry.room.status || "AVAILABLE").toUpperCase();
            var visible = true;

            if (filter === "AVAILABLE") {
                // Show AVAILABLE + RENTED
                visible = (status === "AVAILABLE" || status === "RENTED");
            } else if (filter === "PENDING") {
                // Show PENDING + RENTED
                visible = (status === "PENDING" || status === "RENTED");
            } else if (filter === "RENTED") {
                // Show RENTED only
                visible = (status === "RENTED");
            } else {
                // ALL
                visible = true;
            }

            entry.marker.setVisible(visible);
        });
    }


    /*
     * ============================================================
     * LOCALIZATION
     * ============================================================
     */

    window.roomifyLocalization = {
        explore: "Explore",
        dashboard: "Dashboard",
        savedRooms: "Favourites",
        myBookings: "My Bookings",
        messages: "Messages",
        ownerDashboard: "Owner Dashboard",
        postARoom: "Post a Room",
        adminPanel: "Admin Panel",
        account: "ACCOUNT",
        search: "SEARCH",
        filters: "Filters",
        profile: "My Profile",
        logout: "Logout",
        loginRegister: "Login / Register",
        searchPlaceholder: "Search by area, property name or location",
        monthlyRent: "Monthly Rent",
        beds: "Beds",
        baths: "Baths",
        area: "Area",
        available: "Available",
        pending: "Pending",
        rented: "Rented",
        propertyDetails: "Property Details",
        contactOwner: "Contact Owner",
        language: "Language"
    };

    window.roomifyUpdateLocalization = function(newLocalizationJson) {
        try {
            var newLoc = JSON.parse(newLocalizationJson);
            window.roomifyLocalization = Object.assign({}, window.roomifyLocalization, newLoc);
            console.log("Roomify: Localization updated");

            // Refresh sidebar if it's open
            if (document.getElementById("roomify-sidebar")) {
                window.roomifyUpdateUser(
                    window.roomifyCurrentUser.role,
                    window.roomifyCurrentUser.name,
                    window.roomifyCurrentUser.email,
                    window.roomifyCurrentUser.initials,
                    window.roomifyCurrentUser.profileImage
                );
            }

            // Refresh search placeholder
            var searchInput = document.querySelector(".roomify-search-input");
            if (searchInput) {
                searchInput.placeholder = window.roomifyLocalization.searchPlaceholder;
            }
        } catch (e) {
            console.error("Roomify: Failed to update localization", e);
        }
    };


    /*
     * ============================================================
     * SIDEBAR
     * ============================================================
     */

    function createSidebar() {

        if (document.getElementById("roomify-sidebar")) {
            return;
        }

        var sidebar = document.createElement("aside");
        sidebar.id = "roomify-sidebar";
        sidebar.className = "roomify-sidebar";

        sidebar.innerHTML = `
            <div class="roomify-sidebar-header">
                <div class="roomify-sidebar-brand">Roomify</div>
                <div class="roomify-sidebar-tagline">Your living space partner</div>
            </div>

            <div class="roomify-sidebar-body">
                <div class="roomify-sidebar-section">
                    <div class="roomify-sidebar-label">LOCATION</div>
                    <div class="roomify-search-box">
                         <span class="roomify-search-box-icon">
                            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                         </span>
                         <input type="text" id="sidebar-search-input" placeholder="Enter location e.g. Mbezi, Mikocheni...">
                    </div>
                </div>

                <div class="roomify-sidebar-section">
                    <div class="roomify-sidebar-label">WHAT ARE YOU LOOKING FOR?</div>
                    <div class="roomify-chip-group" id="type-chips">
                        <div class="roomify-chip active" data-type="ALL">All</div>
                        <div class="roomify-chip" data-type="Room">Room</div>
                        <div class="roomify-chip" data-type="Apartment">Apartment</div>
                        <div class="roomify-chip" data-type="Studio">Studio</div>
                        <div class="roomify-chip" data-type="House">House</div>
                        <div class="roomify-chip" data-type="Office">Office</div>
                    </div>
                </div>

                <div class="roomify-sidebar-section">
                    <div class="roomify-sidebar-label">MAX BUDGET</div>
                    <div class="roomify-budget-box">
                        <span class="roomify-budget-icon">
                            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-3"/><path d="M16 12h5v4h-5z"/></svg>
                        </span>
                        <input type="text" id="sidebar-budget-input" placeholder="Max budget e.g. 300000, 500k">
                    </div>
                    <div id="sidebar-filter-btn" style="display: flex; align-items: center; justify-content: center; gap: 8px; background: #1A237E; color: white; border-radius: 12px; padding: 12px; font-weight: 700; cursor: pointer; margin-top: 12px;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon></svg>
                        <span>Filter Results</span>
                    </div>
                </div>

                <div class="roomify-sidebar-section">
                    <div class="roomify-location-btn" id="use-my-location">
                        <span class="roomify-location-icon">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"></polygon></svg>
                        </span>
                        <span>Nearby me</span>
                    </div>
                </div>

                <div class="roomify-sidebar-section">
                    <div class="roomify-sidebar-label">STATUS</div>
                    <div class="roomify-chip-group" id="status-chips">
                        <div class="roomify-chip" data-status="ALL">All</div>
                        <div class="roomify-chip active" data-status="AVAILABLE"><span class="dot green"></span> Available</div>
                        <div class="roomify-chip" data-status="PENDING"><span class="dot yellow"></span> Pending</div>
                        <div class="roomify-chip" data-status="RENTED"><span class="dot red"></span> Rented</div>
                    </div>
                </div>

                <div class="roomify-sidebar-section">
                    <div class="roomify-sidebar-label">EXPLORE FURNITURE</div>
                    <div class="roomify-furniture-card" data-roomify-menu="furniture_dashboard" style="display: flex; align-items: center; gap: 14px; padding: 14px 18px; background: #E8EAF6; border-radius: 16px; border: 1px solid #C7D2FE; cursor: pointer;">
                        <span style="display: flex; align-items: center; justify-content: center; width: 44px; height: 44px; border-radius: 12px; background: #1A237E; color: white;">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 9V6a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2v3"/><path d="M3 16a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-5a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v5z"/><line x1="6" y1="18" x2="6" y2="21"/><line x1="18" y1="18" x2="18" y2="21"/></svg>
                        </span>
                        <div>
                            <div style="font-weight: 700; color: #1A237E; font-size: 15px;">Furniture Hub</div>
                            <div style="font-size: 12px; color: #283593; font-weight: 600;">Furnish Your Space</div>
                        </div>
                    </div>
                </div>

                <div id="dynamic-menu-items"></div>
            </div>

            <div class="roomify-sidebar-footer" id="sidebar-footer-content">
                <!-- Login / Register -->
            </div>
        `;

        document.body.appendChild(sidebar);

        // Quick Search Listener
        const quickSearchInput = document.getElementById("sidebar-search-input");
        if (quickSearchInput) {
            quickSearchInput.addEventListener("input", function() {
                const query = quickSearchInput.value.trim();
                filterRooms(query);
                // Sync with main search if exists
                const mainSearch = document.getElementById("roomify-search-input");
                if (mainSearch) mainSearch.value = query;
            });
        }

        const sidebarFilterBtn = document.getElementById("sidebar-filter-btn");
        if (sidebarFilterBtn) {
            sidebarFilterBtn.onclick = function() {
                const query = quickSearchInput ? quickSearchInput.value.trim() : "";
                filterRooms(query);
                closeSidebar();
            };
        }

        // Chip selection logic
        const setupChips = (containerId, dataAttr, callback) => {
            const container = document.getElementById(containerId);
            if (!container) return;
            container.onclick = (e) => {
                const chip = e.target.closest(".roomify-chip");
                if (!chip) return;
                container.querySelectorAll(".roomify-chip").forEach(c => c.classList.remove("active"));
                chip.classList.add("active");
                if (callback) callback(chip.getAttribute(dataAttr));
            };
        };

        setupChips("type-chips", "data-type", (type) => {
            applySidebarFilters();
        });

        setupChips("status-chips", "data-status", (status) => {
            applySidebarFilters();
        });

        setupChips("beds-chips", "data-beds", (beds) => {
            applySidebarFilters();
        });

        // Budget input listener
        const budgetInput = document.getElementById("sidebar-budget-input");
        if (budgetInput) {
            budgetInput.addEventListener("input", applySidebarFilters);
        }

        function applySidebarFilters() {
            const locEl = document.getElementById("sidebar-search-input");
            const locVal = locEl ? locEl.value.trim() : "";

            const maxBudgetEl = document.getElementById("sidebar-budget-input");
            const rawBudget = maxBudgetEl ? maxBudgetEl.value.trim() : "";
            var clean = rawBudget.toLowerCase().replace("tzs", "").replace(/,/g, "").trim();
            var parsedBudget = null;
            if (clean.endsWith("k")) {
                parsedBudget = parseFloat(clean.slice(0, -1)) * 1000;
            } else if (clean.endsWith("m")) {
                parsedBudget = parseFloat(clean.slice(0, -1)) * 1000000;
            } else {
                parsedBudget = parseFloat(clean);
            }
            if (isNaN(parsedBudget) || parsedBudget <= 0) parsedBudget = null;

            const statusChipsEl = document.getElementById("status-chips");
            const statusActiveEl = statusChipsEl ? statusChipsEl.querySelector(".active") : null;
            const status = statusActiveEl ? statusActiveEl.getAttribute("data-status") : "ALL";

            const typeChipsEl = document.getElementById("type-chips");
            const typeActiveEl = typeChipsEl ? typeChipsEl.querySelector(".active") : null;
            const type = typeActiveEl ? typeActiveEl.getAttribute("data-type") : "ALL";

            var combinedQueryParts = [];
            if (locVal) combinedQueryParts.push(locVal);
            if (parsedBudget) combinedQueryParts.push(parsedBudget);
            if (type && type !== "ALL") combinedQueryParts.push(type);

            filterRooms(combinedQueryParts.join(", "));

            var event = new CustomEvent("roomifySidebarFilter", {
                detail: JSON.stringify({
                    area: locVal || null,
                    maxPrice: parsedBudget,
                    status: (status && status !== "ALL") ? status : null,
                    propertyType: (type && type !== "ALL") ? type : null
                })
            });
            document.dispatchEvent(event);
        }

        // Location button listener
        const locationBtn = document.getElementById("use-my-location");
        if (locationBtn) {
            locationBtn.onclick = function() {
                if (navigator.geolocation) {
                    navigator.geolocation.getCurrentPosition(function(position) {
                        const lat = position.coords.latitude;
                        const lng = position.coords.longitude;
                        if (typeof window.roomifyMoveToRoom === "function") {
                            window.roomifyMoveToRoom(lat, lng);
                        }
                    });
                }
            };
        }

        // Initial setup of sidebar content
        window.roomifyUpdateUser(null, null, null, null);
    }

    /*
     * ============================================================
     * UPDATE SIDEBAR USER STATE
     * ============================================================
     */

    window.roomifyUpdateUser = function(role, name, email, initials, profileImage) {
        console.log("Roomify: Updating sidebar user state", role, name);

        window.roomifyCurrentUser = {
            role: role,
            name: name,
            email: email,
            initials: initials,
            profileImage: profileImage
        };

        var sidebar = document.getElementById("roomify-sidebar");
        if (!sidebar) return;

        var dynamicBody = sidebar.querySelector("#dynamic-menu-items");
        var footerContent = sidebar.querySelector("#sidebar-footer-content");

        if (!dynamicBody || !footerContent) return;

        var loc = window.roomifyLocalization;
        var bodyHtml = "";

        const upperRole = (role || "").toUpperCase();

        if (upperRole === "TENANT") {
            bodyHtml += `
                <div class="roomify-sidebar-section">
                    <div class="roomify-sidebar-label">My Dashboard</div>
                    <div class="roomify-chip-group">
                        <div class="roomify-chip" data-roomify-menu="tenant">Overview</div>
                        <div class="roomify-chip" data-roomify-menu="saved">Favourites</div>
                        <div class="roomify-chip" data-roomify-menu="bookings">My Bookings</div>
                        <div class="roomify-chip" data-roomify-menu="messages">Messages</div>
                    </div>
                </div>
            `;
        } else if (upperRole === "OWNER") {
            bodyHtml += `
                <div class="roomify-sidebar-section">
                    <div class="roomify-sidebar-label">Management</div>
                    <div class="roomify-chip-group">
                        <div class="roomify-chip" data-roomify-menu="ownerdashboard">Dashboard</div>
                        <div class="roomify-chip" data-roomify-menu="postroom">Post a Room</div>
                    </div>
                </div>
            `;
        } else if (upperRole === "DALALI") {
            bodyHtml += `
                <div class="roomify-sidebar-section">
                    <div class="roomify-sidebar-label">Dalali Tools</div>
                    <div class="roomify-chip-group">
                        <div class="roomify-chip" data-roomify-menu="dalalidashboard">Dashboard</div>
                    </div>
                </div>
            `;
        } else if (upperRole === "ADMIN" || upperRole === "SUPER_ADMIN") {
             bodyHtml += `
                <div class="roomify-sidebar-section">
                    <div class="roomify-sidebar-label">Administration</div>
                    <div class="roomify-chip-group">
                        <div class="roomify-chip" data-roomify-menu="admindashboard">Admin Panel</div>
                    </div>
                </div>
            `;
        }

        dynamicBody.innerHTML = bodyHtml;

        // Footer Content
        if (!role) {
            footerContent.innerHTML = `
                <div class="roomify-footer-item" data-roomify-menu="login">
                    <span class="roomify-footer-icon">
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path><polyline points="10 17 15 12 10 7"></polyline><line x1="15" y1="12" x2="3" y2="12"></line></svg>
                    </span>
                    <span>Login / Register</span>
                </div>
            `;
        } else {
            var avatarImg = "";
            if (profileImage && profileImage.indexOf("profile/image") === -1) {
                avatarImg = `<img src="${profileImage}" style="width:32px; height:32px; border-radius:8px; object-fit:cover; margin-right:12px;" />`;
            } else {
                avatarImg = `<div style="width:32px; height:32px; border-radius:8px; background:#E8EAF6; color:#1A237E; display:flex; align-items:center; justify-content:center; font-weight:800; margin-right:12px;">${initials || 'U'}</div>`;
            }

            footerContent.innerHTML = `
                <div class="roomify-footer-item" data-roomify-menu="profile">
                    ${avatarImg}
                    <div style="flex:1; min-width:0;">
                        <div style="font-size:14px; color:#1A1A1A; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${name}</div>
                        <div style="font-size:11px; color:#666;">Account Settings</div>
                    </div>
                </div>
                <div class="roomify-footer-item" data-roomify-menu="logout" style="margin-top:12px; color:#D32F2F;">
                    <span class="roomify-footer-icon">
                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
                    </span>
                    <span>Logout</span>
                </div>
            `;
        }

        // Re-attach listeners for ALL data-roomify-menu items (new and old)
        var allMenuItems = sidebar.querySelectorAll("[data-roomify-menu]");
        allMenuItems.forEach(function (item) {
            if (item && item.parentNode) {
                // Remove old listener if any
                var newItem = item.cloneNode(true);
                item.parentNode.replaceChild(newItem, item);

                newItem.addEventListener("click", function () {
                    var destination = newItem.getAttribute("data-roomify-menu");
                    handleSidebarNavigation(destination);
                });
            }
        });
    };


    function toggleSidebar() {

        var sidebar =
            document.getElementById(
                "roomify-sidebar"
            );

        var backdrop =
            document.getElementById(
                "roomify-sidebar-backdrop"
            );

        if (!sidebar) {
            return;
        }

        if (
            sidebar.classList.contains("open")
        ) {

            closeSidebar();

        } else {

            sidebar.classList.add("open");

            if (backdrop) {
                backdrop.classList.add("open");
            }
        }
    }


    function closeSidebar() {

        var sidebar =
            document.getElementById(
                "roomify-sidebar"
            );

        var backdrop =
            document.getElementById(
                "roomify-sidebar-backdrop"
            );

        if (sidebar) {
            sidebar.classList.remove("open");
        }

        if (backdrop) {
            backdrop.classList.remove("open");
        }
    }


    /*
     * ============================================================
     * SIDEBAR -> KOTLIN NAVIGATION
     * ============================================================
     */

    function handleSidebarNavigation(
        destination
    ) {

        console.log(
            "Roomify: sidebar navigation clicked:",
            destination
        );

        closeSidebar();

        // Dispatch event for Kotlin to handle
        var event =
            new CustomEvent(
                "roomifySidebarNavigation",
                {
                    detail:
                        String(destination)
                }
            );

        document.dispatchEvent(
            event
        );
    }


    /*
     * ============================================================
     * LANGUAGE CHANGE
     * ============================================================
     */

    window.handleLanguageChange = function(langCode) {
        console.log("Roomify: language change requested:", langCode);

        var event = new CustomEvent("roomifyLanguageChange", {
            detail: String(langCode)
        });

        document.dispatchEvent(event);
    };


    /*
     * ============================================================
     * INSTALL MAP CONTROLS
     * ============================================================
     */

    function installMapControls() {

        if (
            !window.roomifyMap ||
            !window.google ||
            !window.google.maps
        ) {
            return;
        }

        injectRoomifyControlStyles();

        createSidebar();

        var searchControl =
            createSearchControl();

        var menuControl =
            createMenuControl();

        var topRightControls =
            createTopRightControls();


        /*
         * TOP CENTER
         *
         * Search is owned by Google Maps.
         */
        if (
            searchControl &&
            !window.roomifySearchControlInstalled
        ) {

            window.roomifyMap.controls[
                google.maps.ControlPosition.TOP_CENTER
                ].push(
                searchControl
            );

            window.roomifySearchControlInstalled =
                true;
        }


        /*
         * TOP LEFT
         *
         * Menu is owned by Google Maps.
         */
        if (
            menuControl &&
            !window.roomifyMenuControlInstalled &&
            false // Disabled for static sidebar
        ) {

            window.roomifyMap.controls[
                google.maps.ControlPosition.TOP_LEFT
                ].push(
                menuControl
            );

            window.roomifyMenuControlInstalled =
                true;
        }

        /*
         * TOP RIGHT
         *
         * Dropdowns are owned by Google Maps.
         */
        if (
            topRightControls &&
            !window.roomifyStatusControlInstalled
        ) {

            window.roomifyMap.controls[
                google.maps.ControlPosition.TOP_RIGHT
                ].push(
                topRightControls
            );

            window.roomifyStatusControlInstalled =
                true;
        }
    }


    /*
     * ============================================================
     * FILTER ROOMS
     * ============================================================
     */

    function filterRooms(query) {
        var normalized = String(query || "").trim().toLowerCase();

        if (!normalized) {
            updateVisibleMarkers(window.roomifyAllRooms);
            hideSearchCount();
            return;
        }

        var validTypes = ["room", "apartment", "studio", "house", "office"];
        var targetType = null;
        var maxBudget = null;
        var locationTerms = [];

        var parts = normalized.includes(",") ? normalized.split(",") : normalized.split(/\s+/);

        parts.forEach(function(part) {
            var trimmed = part.trim();
            if (!trimmed) return;

            if (validTypes.includes(trimmed) && !targetType) {
                targetType = trimmed;
                return;
            }

            var clean = trimmed.replace("tzs", "").replace(/,/g, "").trim();
            var priceNum = null;
            if (clean.endsWith("k")) {
                priceNum = parseFloat(clean.slice(0, -1)) * 1000;
            } else if (clean.endsWith("m")) {
                priceNum = parseFloat(clean.slice(0, -1)) * 1000000;
            } else {
                priceNum = parseFloat(clean);
            }

            if (!isNaN(priceNum) && priceNum > 0 && maxBudget === null) {
                maxBudget = priceNum;
                return;
            }

            locationTerms.push(trimmed);
        });

        var filtered = window.roomifyAllRooms.filter(function(room) {
            if (maxBudget !== null && room.price > maxBudget) {
                return false;
            }

            if (targetType) {
                var pType = String(room.propertyType || "").toLowerCase();
                if (!pType.includes(targetType)) return false;
            }

            if (locationTerms.length > 0) {
                var searchable = [
                    room.title, room.address, room.city, room.areaName, room.location
                ].filter(Boolean).join(" ").toLowerCase();

                return locationTerms.every(function(term) {
                    return searchable.includes(term);
                });
            }

            return true;
        });

        updateVisibleMarkers(filtered);
        showSearchCount(filtered.length, window.roomifyAllRooms.length);

        if (filtered.length > 0 && window.roomifyMap) {
            var bounds = new google.maps.LatLngBounds();
            filtered.forEach(function(r) {
                if (r.latitude && r.longitude) {
                    bounds.extend(new google.maps.LatLng(Number(r.latitude), Number(r.longitude)));
                }
            });
            if (!bounds.isEmpty()) {
                window.roomifyMap.fitBounds(bounds);
                if (filtered.length === 1 && window.roomifyMap.getZoom() > 16) {
                    window.roomifyMap.setZoom(16);
                }
            }
        }
    }


    /*
     * ============================================================
     * UPDATE VISIBLE MARKERS
     * ============================================================
     */

    function updateVisibleMarkers(
        rooms
    ) {

        if (
            !window.roomifyMap
        ) {
            return;
        }

        /*
         * Remove current markers.
         */

        if (
            Array.isArray(
                window.roomifyMarkers
            )
        ) {

            window.roomifyMarkers.forEach(
                function (entry) {

                    if (
                        entry &&
                        entry.marker
                    ) {

                        entry.marker.setMap(
                            null
                        );
                    }
                }
            );
        }

        window.roomifyMarkers = [];


        /*
         * Recreate only matching markers.
         */

        rooms.forEach(
            function (room) {

                createRoomMarker(
                    room
                );
            }
        );

        // Ensure status filter is applied to newly created markers
        applyMarkerVisibility();
    }


    /*
     * ============================================================
     * SEARCH COUNT
     * ============================================================
     */

    function showSearchCount(
        count,
        total
    ) {

        var element =
            document.getElementById(
                "roomify-search-count"
            );

        if (!element) {

            element =
                document.createElement(
                    "div"
                );

            element.id =
                "roomify-search-count";

            element.className =
                "roomify-search-count";

            document.body.appendChild(
                element
            );
        }

        element.textContent =
            count +
            " of " +
            total +
            " properties";

        element.style.display =
            "block";
    }


    function hideSearchCount() {

        var element =
            document.getElementById(
                "roomify-search-count"
            );

        if (element) {

            element.style.display =
                "none";
        }
    }


    /*
     * ============================================================
     * CREATE MAP
     * ============================================================
     */

    window.roomifyCreateMap =
        function () {

            console.log(
                "Roomify: createMap() called"
            );

            try {
                var container =
                    document.getElementById(
                        "google-map-container"
                    );

                if (!container) {

                    console.error(
                        "Roomify: map container not found"
                    );

                    return;
                }

                if (
                    !window.google ||
                    !window.google.maps
                ) {

                    console.error(
                        "Roomify: Google Maps API not loaded"
                    );

                    return;
                }

                if (
                    window.roomifyMap
                ) {

                    console.log(
                        "Roomify: map already exists"
                    );

                    installMapControls();

                    return;
                }


                container.style.display =
                    "block";

                container.style.visibility =
                    "visible";

                container.style.opacity =
                    "1";

                container.style.zIndex =
                    "9999";


                window.roomifyMap =
                    new google.maps.Map(
                        container,
                        {

                            center: {
                                lat: -6.7924,
                                lng: 39.2083
                            },

                            zoom: 11,

                            mapTypeId:
                                "roadmap",

                            draggable:
                                true,

                            gestureHandling:
                                "greedy",

                            scrollwheel:
                                true,

                            disableDoubleClickZoom:
                                false,

                            zoomControl:
                                true,

                            fullscreenControl:
                                true,

                            streetViewControl:
                                false,

                            mapTypeControl:
                                false,

                            rotateControl:
                                false,

                            clickableIcons:
                                false,

                            tilt:
                                0,

                            styles:
                                MAP_STYLE_DETAILED
                        }
                    );

                window.roomifyMap.addListener("zoom_changed", function() {
                    console.log("Roomify: zoom changed to " + window.roomifyMap.getZoom());
                    refreshMarkerIcons();
                });

                /*
                 * Click on map background:
                 *
                 * 1. Unpin the popup
                 * 2. Hide the info window
                 * 3. Clear selected room
                 */
                window.roomifyMap.addListener(
                    "click",
                    function () {

                        console.log(
                            "Roomify: map clicked, closing popup"
                        );

                        window.roomifyPopupPinned = false;

                        if (
                            window.roomifyInfoOverlay
                        ) {

                            window.roomifyInfoOverlay.hide();
                        }

                        window.roomifySelectedRoomId = null;

                        refreshMarkerIcons();
                    }
                );

                createRoomInfoOverlay();

                /*
                 * IMPORTANT:
                 *
                 * Install Search + Menu as Google Maps controls.
                 */
                installMapControls();


                console.log(
                    "Roomify: Google Map created successfully"
                );
            } catch (err) {
                console.error("Roomify: Error creating Google Map:", err);
            }
        };


    window.roomifyFitMapToRooms = function(roomsJson) {
        if (!window.roomifyMap || !window.google || !window.google.maps) return;

        var rooms = [];
        try {
            rooms = JSON.parse(roomsJson);
        } catch (e) {
            console.error("Roomify: Failed to parse rooms for fitBounds", e);
            return;
        }

        if (rooms.length === 0) return;

        var bounds = new google.maps.LatLngBounds();
        var validCount = 0;

        rooms.forEach(function(room) {
            var lat = Number(room.latitude !== undefined ? room.latitude : room.lat);
            var lng = Number(room.longitude !== undefined ? room.longitude : room.lng);

            if (isFinite(lat) && isFinite(lng) && lat !== 0 && lng !== 0) {
                bounds.extend(new google.maps.LatLng(lat, lng));
                validCount++;
            }
        });

        if (validCount > 0) {
            console.log("Roomify: Fitting map to " + validCount + " markers");
            window.roomifyMap.fitBounds(bounds);

            // If only one room, fitBounds might zoom in too much or too little depending on implementation.
            // Usually Google Maps handles it well, but if zoom is too high, we can cap it.
            var listener = google.maps.event.addListener(window.roomifyMap, "idle", function() {
                if (window.roomifyMap.getZoom() > 16) window.roomifyMap.setZoom(16);
                google.maps.event.removeListener(listener);
            });
        }
    };


    /*
     * ============================================================
     * FULL PRICE
     * ============================================================
     */

    function formatFullPrice(price) {

        var value =
            Number(price);

        if (
            !isFinite(value) ||
            value <= 0
        ) {

            return "Price on request";
        }

        return (
            "TZS " +
            Math.round(value)
                .toLocaleString("en-US") +
            "/month"
        );
    }


    /*
     * ============================================================
     * COMPACT PRICE
     * ============================================================
     */

    function formatCompactPrice(price) {

        var value =
            Number(price);

        if (
            !isFinite(value) ||
            value <= 0
        ) {

            return "Price on request";
        }

        if (
            value >= 1000000
        ) {

            var millions =
                Math.round(
                    (value / 1000000) * 100
                ) / 100;

            return (
                "TZS " +
                millions +
                "M"
            );
        }

        if (
            value >= 1000
        ) {

            var thousands =
                Math.round(
                    (value / 1000) * 100
                ) / 100;

            return (
                "TZS " +
                thousands +
                "K"
            );
        }

        return (
            "TZS " +
            Math.round(value)
        );
    }


    /*
     * ============================================================
     * STATUS
     * ============================================================
     */

    function statusText(status) {

        var normalized =
            String(
                status || "AVAILABLE"
            ).toUpperCase();

        switch (normalized) {

            case "AVAILABLE":
                return "Available";

            case "PENDING":
                return "Pending";

            case "RENTED":
                return "Rented";

            default:
                return (
                    normalized.charAt(0) +
                    normalized.slice(1)
                        .toLowerCase()
                );
        }
    }


    function statusColor(status) {

        var normalized =
            String(
                status || "AVAILABLE"
            ).toUpperCase();

        switch (normalized) {

            case "AVAILABLE":
                return "#2E7D32";

            case "PENDING":
                return "#F9A825";

            case "RENTED":
                return "#C62828";

            default:
                return "#1976D2";
        }
    }


    function statusAlpha(status) {

        var normalized =
            String(
                status || "AVAILABLE"
            ).toUpperCase();

        if (
            normalized === "RENTED"
        ) {

            return 0.55;
        }

        return 1.0;
    }


    /*
     * ============================================================
     * ESCAPE HTML
     * ============================================================
     */

    function escapeHtml(value) {

        return String(value || "")
            .replace(
                /&/g,
                "&amp;"
            )
            .replace(
                /</g,
                "&lt;"
            )
            .replace(
                />/g,
                "&gt;"
            )
            .replace(
                /"/g,
                "&quot;"
            )
            .replace(
                /'/g,
                "&#039;"
            );
    }


    /*
     * ============================================================
     * PRICE ICON
     * ============================================================
     */

    function createPriceIcon(
        price,
        status,
        selected,
        isComplex,
        isViewed,
        isSaved
    ) {
        var text = formatCompactPrice(price);

        var color = selected ? ROOMIFY_SELECTED_ORANGE : (isSaved ? ROOMIFY_SAVED_PINK : statusColor(status));
        if (isViewed && !selected && !isSaved) color = ROOMIFY_VIEWED_GRAY;

        var alpha = selected ? 1.0 : statusAlpha(status);

        // Growth effect for selected marker
        var scale = selected ? 1.35 : 1.0;
        var width = 120 * scale;
        var height = 55 * scale;

        var vectorIconSvg = isComplex
            ? '<g transform="translate(19, 15)"><path d="M2 18V3a1 1 0 0 1 1-1h12a1 1 0 0 1 1 1v15H2zm3-13h3v2.5H5V5zm0 4h3v2.5H5V9zm0 4h3v2.5H5V13zm5-8h3v2.5h-3V5zm0 4h3v2.5h-3V9zm0 4h3v2.5h-3V13z" fill="#FFFFFF"/></g>'
            : '<g transform="translate(19, 15.5)"><path d="M9 1.5L1 8h2.5v8.5a1 1 0 0 0 1 1h10a1 1 0 0 0 1-1V8H17L9 1.5zM7 15.5v-4h4v4H7z" fill="#FFFFFF"/></g>';

        var svg =
            '<svg xmlns="http://www.w3.org/2000/svg" ' +
            'width="' + width + '" height="' + height + '" ' +
            'viewBox="0 0 120 55">' +
            '<defs>' +
            '<filter id="shadow" x="-50%" y="-50%" width="200%" height="200%">' +
                '<feDropShadow dx="0" dy="1.5" stdDeviation="2.5" flood-opacity="0.35"/>' +
            '</filter>' +
            '</defs>' +
            '<g filter="url(#shadow)" opacity="' + alpha + '">' +
                '<rect x="5" y="5" rx="18" ry="18" width="110" height="38" fill="' + color + '"/>' +
                '<path d="M53 43 L60 52 L67 43" fill="' + color + '"/>' +
                // Subtle background circle for the icon to make it pop
                '<circle cx="28" cy="24" r="14" fill="rgba(255,255,255,0.18)"/>' +
                vectorIconSvg +
                '<text x="72" y="30" text-anchor="middle" font-family="Arial,sans-serif" font-size="13" font-weight="900" fill="#ffffff">' +
                    escapeHtml(text) +
                '</text>' +
            '</g>' +
            '</svg>';

        return {
            url: "data:image/svg+xml;charset=UTF-8," + encodeURIComponent(svg),
            scaledSize: new google.maps.Size(width, height),
            anchor: new google.maps.Point(width / 2, height - 2)
        };
    }

    function createDotIcon(status, selected, hovered, isViewed, isSaved) {
        var size = 10;
        if (selected) size = 16;
        else if (hovered) size = 14;

        var color = selected ? ROOMIFY_SELECTED_ORANGE : (isSaved ? ROOMIFY_SAVED_PINK : statusColor(status));
        if (isViewed && !selected && !isSaved) color = ROOMIFY_VIEWED_GRAY;

        var svg =
            '<svg xmlns="http://www.w3.org/2000/svg" width="' + (size + 6) + '" height="' + (size + 6) + '" viewBox="0 0 ' + (size + 6) + ' ' + (size + 6) + '">' +
            '<circle cx="' + (size/2 + 3) + '" cy="' + (size/2 + 3) + '" r="' + (size/2) + '" fill="' + color + '" stroke="#FFFFFF" stroke-width="2"/>' +
            '</svg>';

        return {
            url: "data:image/svg+xml;charset=UTF-8," + encodeURIComponent(svg),
            scaledSize: new google.maps.Size(size + 6, size + 6),
            anchor: new google.maps.Point(size/2 + 3, size/2 + 3)
        };
    }


    /*
     * ============================================================
     * BUILD POPUP CONTENT - ANDROID STYLE (WITH FIX)
     * ============================================================
     */

    function buildPopupContent(room) {

        var title =
            escapeHtml(
                room.title || "Room"
            );

        var price =
            escapeHtml(
                formatFullPrice(
                    room.price
                )
            );

        var status =
            String(
                room.status ||
                "AVAILABLE"
            ).toUpperCase();

        var statusLabel =
            escapeHtml(
                statusText(status)
            );

        var statusColorValue =
            statusColor(status);

        var address =
            escapeHtml(
                room.address || ""
            );

        var propertyType =
            escapeHtml(
                room.propertyType || ""
            );

        var locationDisplay =
            address || "Location available";

        var imageHtml = "";
        if (room.image) {
            imageHtml =
                '<div class="roomify-popup-image-container">' +
                '<img src="' + escapeHtml(room.image) + '" class="roomify-popup-image" onerror="this.style.display=\'none\'" />' +
                '</div>';
        }

        var featuresHtml = "";

        var features = [];

        if (
            Number(room.roomsCount) > 0
        ) {

            features.push(
                Number(room.roomsCount) +
                " Beds"
            );
        }

        if (
            Number(room.bathroomsCount) > 0
        ) {

            features.push(
                Number(room.bathroomsCount) +
                " Baths"
            );
        }

        if (
            Number(room.area) > 0
        ) {

            features.push(
                Math.round(
                    Number(room.area)
                ) +
                " m²"
            );
        }

        if (
            features.length > 0
        ) {

            featuresHtml =
                '<div class="roomify-popup-features">';

            features.forEach(
                function (feature) {

                    var icon = "";

                    if (
                        feature.indexOf("Beds") > -1
                    ) {
                        icon = "";
                    } else if (
                        feature.indexOf("Baths") > -1
                    ) {
                        icon = "";
                    } else if (
                        feature.indexOf("m²") > -1
                    ) {
                        icon = "";
                    }

                    featuresHtml +=
                        '<span class="roomify-popup-feature">' +
                        '<span class="roomify-popup-feature-icon">' +
                        icon +
                        '</span>' +
                        feature +
                        '</span>';
                }
            );

            featuresHtml += '</div>';
        }

        var isRented = status === "RENTED";
        var buttonText = isRented ? "RENTED - View Details" : "View Property Details";
        var buttonStyle = isRented ? 'style="opacity:0.8; background:#E0E0E0; color:#757575;"' : "";

        // FIX: Wrap everything in a container that stops propagation
        return (
            '<div class="roomify-popup-container-wrapper" ' +
            'onclick="event.stopPropagation();" ' +
            'onmousedown="event.stopPropagation();" ' +
            'onmouseup="event.stopPropagation();">' +

            '<div class="roomify-popup">' +
            (isRented ? '<div style="position:absolute; top:10px; left:10px; background:rgba(198,40,40,0.9); color:white; padding:2px 8px; border-radius:10px; font-size:9px; font-weight:bold; z-index:10;">RENTED</div>' : '') +

            '<div class="roomify-popup-header">' +

            '<div style="flex:1;min-width:0;">' +
            '<div class="roomify-popup-property-type">' +
            (propertyType || "Property") +
            '</div>' +
            '<div class="roomify-popup-title" title="' +
            title +
            '">' +
            title +
            '</div>' +
            '</div>' +

            '<button class="roomify-popup-close" data-roomify-close-popup="true">' +
            '✕' +
            '</button>' +

            '</div>' +

            imageHtml +

            '<div class="roomify-popup-location">' +
            '<span class="roomify-popup-location-text" title="' +
            locationDisplay +
            '">' +
            locationDisplay +
            '</span>' +
            '</div>' +

            '<div class="roomify-popup-price-row">' +
            '<div>' +
            '<div class="roomify-popup-price-label">MONTHLY RENT</div>' +
            '<div class="roomify-popup-price">' +
            price +
            '</div>' +
            '</div>' +
            '<div class="roomify-popup-status">' +
            '<span class="roomify-popup-status-dot" style="background:' +
            statusColorValue +
            ';"></span>' +
            statusLabel +
            '</div>' +
            '</div>' +

            featuresHtml +

            '<button class="roomify-popup-button" ' + buttonStyle + ' data-roomify-view-details="' +
            escapeHtml(room.id) +
            '">' +
            buttonText +
            '</button>' +

            '</div>' +

            '</div>'
        );
    }


    /*
     * ============================================================
     * CUSTOM OVERLAY
     * ============================================================
     */

    function createRoomInfoOverlay() {

        if (
            window.roomifyInfoOverlay
        ) {
            return;
        }


        class RoomInfoOverlay
            extends google.maps.OverlayView {

            constructor() {

                super();

                this.position = null;
                this.content = "";
                this.div = null;
                this.roomId = null;
            }


            onAdd() {

                this.div =
                    document.createElement(
                        "div"
                    );

                this.div.style.position =
                    "absolute";

                this.div.style.zIndex =
                    "1000";

                this.div.style.pointerEvents =
                    "auto";

                this.div.style.display =
                    "none";

                this.div.className =
                    "roomify-popup-container";

                this.div.innerHTML =
                    this.content;

                this.getPanes()
                    .floatPane
                    .appendChild(
                        this.div
                    );

                this.attachListeners();
            }


            draw() {

                if (
                    !this.div ||
                    !this.position
                ) {
                    return;
                }

                var projection =
                    this.getProjection();

                if (!projection) {
                    return;
                }

                var point =
                    projection.fromLatLngToDivPixel(
                        this.position
                    );

                if (!point) {
                    return;
                }

                this.div.style.left =
                    point.x + "px";

                this.div.style.top =
                    (point.y - 15) + "px";

                this.div.style.transform =
                    "translate(-50%, -100%)";

                this.div.style.display =
                    "block";

                // Add arrow pointer
                this.div.style.setProperty(
                    "--popup-arrow",
                    "block"
                );
            }


            onRemove() {

                if (
                    this.div
                ) {

                    this.div.remove();

                    this.div = null;
                }
            }


            setPosition(position) {

                if (!position) {
                    return;
                }

                if (
                    typeof position.lat ===
                    "function" &&
                    typeof position.lng ===
                    "function"
                ) {

                    this.position =
                        position;

                } else {

                    this.position =
                        new google.maps.LatLng(
                            Number(
                                position.lat
                            ),
                            Number(
                                position.lng
                            )
                        );
                }

                if (
                    this.div
                ) {

                    this.draw();
                }
            }


            setContent(content, roomId) {

                this.content =
                    content;

                this.roomId =
                    roomId;

                if (
                    this.div
                ) {

                    this.div.innerHTML =
                        content;

                    this.attachListeners();
                }
            }


            show() {

                if (
                    this.div
                ) {

                    this.div.style.display =
                        "block";

                    this.draw();

                    this.attachListeners();
                }
            }


            hide() {

                if (
                    this.div
                ) {

                    this.div.style.display =
                        "none";
                }
            }


            attachListeners() {

                if (
                    !this.div
                ) {
                    return;
                }

                // Prevent clicks on the entire popup from bubbling to the map
                this.div.addEventListener('click', function(event) {
                    event.stopPropagation();
                    event.cancelBubble = true;
                });

                this.div.addEventListener('mousedown', function(event) {
                    event.stopPropagation();
                    event.cancelBubble = true;
                });

                this.div.addEventListener('mouseup', function(event) {
                    event.stopPropagation();
                    event.cancelBubble = true;
                });

                // Close button listener
                var closeBtn =
                    this.div.querySelector(
                        "[data-roomify-close-popup]"
                    );

                if (closeBtn) {

                    closeBtn.onclick =
                        function (event) {

                            event.preventDefault();
                            event.stopPropagation();
                            event.cancelBubble = true;

                            console.log(
                                "Roomify: closing popup"
                            );

                            window.roomifyPopupPinned =
                                false;

                            if (
                                window.roomifyInfoOverlay
                            ) {

                                window.roomifyInfoOverlay.hide();
                            }

                            window.roomifySelectedRoomId =
                                null;

                            refreshMarkerIcons();
                        };
                }

                // View Details button listener
                var viewBtn =
                    this.div.querySelector(
                        "[data-roomify-view-details]"
                    );

                if (viewBtn) {

                    viewBtn.onclick =
                        function (event) {

                            event.preventDefault();
                            event.stopPropagation();
                            event.cancelBubble = true;

                            var roomId =
                                viewBtn.getAttribute(
                                    "data-roomify-view-details"
                                );

                            if (!roomId) {
                                return;
                            }

                            console.log(
                                "Roomify: View Details clicked:",
                                roomId
                            );

                            // CLOSE THE POPUP FIRST
                            if (
                                window.roomifyInfoOverlay
                            ) {
                                window.roomifyInfoOverlay.hide();
                            }
                            window.roomifyPopupPinned = false;

                            // Dispatch event for Kotlin to handle navigation
                            var customEvent =
                                new CustomEvent(
                                    "roomViewDetails",
                                    {
                                        detail:
                                            String(
                                                roomId
                                            )
                                    }
                                );

                            console.log(
                                "Roomify: Dispatching roomViewDetails event"
                            );

                            document.dispatchEvent(
                                customEvent
                            );
                        };
                }
            }
        }


        window.roomifyInfoOverlay =
            new RoomInfoOverlay();

        window.roomifyInfoOverlay.setMap(
            window.roomifyMap
        );
    }


    function closeInfoWindow() {

        window.roomifyPopupPinned = false;

        if (
            window.roomifyInfoOverlay
        ) {

            window.roomifyInfoOverlay.hide();
        }
    }


    function showRoomInfo(
        marker,
        room
    ) {

        if (
            !window.roomifyInfoOverlay
        ) {

            createRoomInfoOverlay();
        }

        if (
            !window.roomifyInfoOverlay
        ) {
            return;
        }

        var content =
            buildPopupContent(
                room
            );

        window.roomifyInfoOverlay.setContent(
            content,
            String(room.id)
        );


        var position =
            marker.getPosition();

        if (
            !position
        ) {
            return;
        }


        window.roomifyInfoOverlay.setPosition(
            position
        );

        window.roomifyInfoOverlay.show();

        /*
         * IMPORTANT:
         *
         * Add mouse leave listener to hide popup when cursor leaves.
         */
        var overlayDiv = window.roomifyInfoOverlay.div;

        if (overlayDiv) {

            // Remove any existing listeners to prevent duplicates
            overlayDiv.onmouseleave = null;

            overlayDiv.onmouseleave = function () {

                console.log(
                    "Roomify: mouse left popup, hiding"
                );

                // Only hide if not clicked (selected)
                // If a marker was clicked, we want to keep it open
                // until user clicks elsewhere
                if (!window.roomifyPopupPinned) {

                    window.roomifyInfoOverlay.hide();
                }
            };
        }
    }


    /*
     * ============================================================
     * REFRESH MARKER ICONS
     * ============================================================
     */

    function refreshMarkerIcons() {
        if (!Array.isArray(window.roomifyMarkers) || !window.roomifyMap) return;

        var zoom = window.roomifyMap.getZoom();
        var showClusters = zoom < 7;
        var showDots = zoom < 7;
        var showPills = zoom >= 7;

        window.roomifyMarkers.forEach(function (entry) {
            if (!entry) return;

            var selected = String(entry.room.id) === String(window.roomifySelectedRoomId);
            var hovered = entry.hovered || false;
            var isViewed = window.roomifyViewedRoomIds.has(Number(entry.room.id));
            var isSaved = window.roomifySavedRoomIds.has(Number(entry.room.id));

            var icon;
            if (showDots) {
                icon = createDotIcon(entry.room.status, selected, hovered, isViewed, isSaved);
            } else {
                icon = createPriceIcon(
                    entry.room.price,
                    entry.room.status,
                    selected,
                    !!entry.room.propertyId,
                    isViewed,
                    isSaved
                );
            }

            entry.marker.setIcon(icon);

            // MarkerClusterer handling
            var clustererLib = window.markerClusterer || window.MarkerClusterer;
            if (showClusters) {
                if (!window.roomifyMarkerClusterer && clustererLib) {
                    initMarkerClusterer();
                }
            } else {
                if (window.roomifyMarkerClusterer) {
                    window.roomifyMarkerClusterer.clearMarkers();
                    window.roomifyMarkerClusterer = null;
                    // Restore markers to map if they were removed by clusterer
                    window.roomifyMarkers.forEach(m => m && m.marker && m.marker.setMap(window.roomifyMap));
                }
            }
        });
    }

    function initMarkerClusterer() {
        if (!window.roomifyMap || !window.roomifyMarkers || !window.roomifyMarkers.length) return;

        var clustererLib = window.markerClusterer || window.MarkerClusterer;
        if (!clustererLib) return;

        var markers = window.roomifyMarkers.map(function(m) { return m.marker; });

        var ClustererClass = clustererLib.MarkerClusterer || clustererLib;
        window.roomifyMarkerClusterer = new ClustererClass({
            map: window.roomifyMap,
            markers: markers,
            renderer: {
                render: function(cluster, stats) {
                    var count = cluster.count;
                    var color = ROOMIFY_BLUE;
                    var size = 50;

                    var svg =
                        '<svg xmlns="http://www.w3.org/2000/svg" width="' + size + '" height="' + size + '">' +
                        '<circle cx="' + size/2 + '" cy="' + size/2 + '" r="' + (size/2 - 2) + '" fill="' + color + '" stroke="#FFFFFF" stroke-width="3"/>' +
                        '<text x="50%" y="50%" text-anchor="middle" dy=".3em" font-family="Arial,sans-serif" font-size="16" font-weight="900" fill="#FFFFFF">' + count + '</text>' +
                        '</svg>';

                    return new google.maps.Marker({
                        position: cluster.position,
                        icon: {
                            url: "data:image/svg+xml;charset=UTF-8," + encodeURIComponent(svg),
                            scaledSize: new google.maps.Size(size, size),
                            anchor: new google.maps.Point(size/2, size/2)
                        },
                        label: "",
                        zIndex: Number(google.maps.Marker.MAX_ZINDEX) + count
                    });
                }
            }
        });
    }


    window.roomifySelectRoom =
        function (roomId) {
            window.roomifySelectedRoomId = String(roomId);
            window.roomifyViewedRoomIds.add(Number(roomId));
            refreshMarkerIcons();
        };

    window.roomifyUpdateViewedRooms = function(idsJson) {
        try {
            var ids = JSON.parse(idsJson);
            window.roomifyViewedRoomIds = new Set(ids.map(Number));
            refreshMarkerIcons();
        } catch(e) {}
    };

    window.roomifyUpdateSavedRooms = function(idsJson) {
        try {
            var ids = JSON.parse(idsJson);
            window.roomifySavedRoomIds = new Set(ids.map(Number));
            refreshMarkerIcons();
        } catch(e) {}
    };


    /*
     * ============================================================
     * CREATE SINGLE ROOM MARKER
     * ============================================================
     */

    function createRoomMarker(room) {

        var latitude =
            room.latitude !== undefined
                ? Number(
                    room.latitude
                )
                : Number(
                    room.lat
                );


        var longitude =
            room.longitude !== undefined
                ? Number(
                    room.longitude
                )
                : Number(
                    room.lng
                );


        if (
            !isFinite(latitude) ||
            !isFinite(longitude) ||
            latitude === 0 ||
            longitude === 0
        ) {

            console.warn(
                "Roomify: invalid coordinates for room",
                room.id
            );

            return;
        }


        var position = {
            lat:
            latitude,

            lng:
            longitude
        };


        var selected =
            String(room.id) ===
            String(
                window.roomifySelectedRoomId
            );


        var marker =
            new google.maps.Marker(
                {
                    position:
                    position,

                    map:
                    window.roomifyMap,

                    title:
                        room.title ||
                        "Room",

                    icon:
                        createPriceIcon(
                            room.price,
                            room.status,
                            selected,
                            !!room.propertyId,
                            window.roomifyViewedRoomIds.has(Number(room.id)),
                            window.roomifySavedRoomIds.has(Number(room.id))
                        ),

                    optimized:
                        true,

                    animation: google.maps.Animation.DROP
                }
            );


        /*
         * Desktop hover.
         */

        marker.addListener(
            "mouseover",
            function () {
                var entry = window.roomifyMarkers.find(m => m.marker === marker);
                if (entry) entry.hovered = true;
                refreshMarkerIcons();

                showRoomInfo(
                    marker,
                    room
                );
            }
        );

        marker.addListener(
            "mouseout",
            function () {
                var entry = window.roomifyMarkers.find(m => m.marker === marker);
                if (entry) entry.hovered = false;
                refreshMarkerIcons();
            }
        );


        /*
         * Touch / click.
         */

        marker.addListener(
            "click",
            function () {

                console.log(
                    "Roomify: marker clicked:",
                    room.id
                );


                window.roomifySelectedRoomId =
                    String(room.id);

                window.roomifyViewedRoomIds.add(Number(room.id));

                /*
                 * Pin the popup when clicked.
                 *
                 * This prevents it from hiding on mouse leave.
                 */
                window.roomifyPopupPinned = true;


                refreshMarkerIcons();


                showRoomInfo(
                    marker,
                    room
                );


                document.dispatchEvent(
                    new CustomEvent(
                        "roomMarkerClicked",
                        {
                            detail:
                                String(
                                    room.id
                                )
                        }
                    )
                );
            }
        );


        window.roomifyMarkers.push(
            {
                marker:
                marker,

                room:
                room
            }
        );
    }


    /*
     * ============================================================
     * CLEAR MARKERS
     * ============================================================
     */

    window.roomifyClearMarkers =
        function () {

            closeInfoWindow();

            window.roomifySelectedRoomId =
                null;


            if (
                Array.isArray(
                    window.roomifyMarkers
                )
            ) {

                window.roomifyMarkers.forEach(
                    function (entry) {

                        if (
                            entry &&
                            entry.marker
                        ) {

                            entry.marker.setMap(
                                null
                            );
                        }
                    }
                );
            }


            window.roomifyMarkers = [];
        };


    /*
     * ============================================================
     * UPDATE MARKERS
     * ============================================================
     */

    window.roomifyUpdateMarkers =
        function (roomsJson) {

            console.log(
                "Roomify: updateMarkers() called"
            );

            try {
                if (
                    !window.roomifyMap
                ) {

                    console.warn(
                        "Roomify: map is not ready"
                    );

                    return;
                }


                var rooms;


                try {

                    rooms =
                        JSON.parse(
                            roomsJson
                        );

                } catch (error) {

                    console.error(
                        "Roomify: invalid rooms JSON",
                        error
                    );

                    return;
                }


                if (
                    !Array.isArray(
                        rooms
                    )
                ) {

                    console.error(
                        "Roomify: rooms data is not an array"
                    );

                    return;
                }


                /*
                 * IMPORTANT:
                 *
                 * Store the complete room list.
                 *
                 * Search operates on this list.
                 */

                window.roomifyAllRooms =
                    rooms;


                /*
                 * If a search is active,
                 * immediately apply it.
                 */

                if (
                    window.roomifySearchQuery
                ) {

                    filterRooms(
                        window.roomifySearchQuery
                    );

                    return;
                }


                updateVisibleMarkers(
                    rooms
                );

                // Re-apply status filter visibility
                applyMarkerVisibility();


                console.log(
                    "Roomify: created " +
                    (window.roomifyMarkers ? window.roomifyMarkers.length : 0) +
                    " markers"
                );
            } catch (err) {
                console.error("Roomify: Error in updateMarkers:", err);
            }
        };


    /*
     * ============================================================
     * MOVE TO ROOM
     * ============================================================
     */

    window.roomifyMoveToRoom =
        function (
            latitude,
            longitude
        ) {

            if (
                !window.roomifyMap
            ) {
                return;
            }


            var lat =
                Number(latitude);

            var lng =
                Number(longitude);


            if (
                !isFinite(lat) ||
                !isFinite(lng)
            ) {
                return;
            }


            window.roomifyMap.panTo(
                {
                    lat:
                    lat,

                    lng:
                    lng
                }
            );


            window.roomifyMap.setZoom(
                15
            );
        };


    /*
     * ============================================================
     * DESTROY MAP
     * ============================================================
     */

    window.roomifyDestroyMap =
        function () {

            console.log(
                "Roomify: destroying map"
            );


            var map =
                document.getElementById(
                    "google-map-container"
                );


            if (map) {

                map.style.display =
                    "none";

                map.style.visibility =
                    "hidden";

                map.style.opacity =
                    "0";

                map.style.zIndex =
                    "-1";
            }


            window.roomifyClearMarkers();


            if (
                window.roomifyInfoOverlay
            ) {

                window.roomifyInfoOverlay.setMap(
                    null
                );

                window.roomifyInfoOverlay =
                    null;
            }


            window.roomifyAllRooms =
                [];

            window.roomifySearchQuery =
                "";

            window.roomifyMap =
                null;


            console.log(
                "Roomify: map destroyed"
            );
        };


    console.log(
        "[ROOMIFY MAP] roomify-map.js loaded - v3.0 (Pixel-Perfect Squeezed Layout)"
    );

})();
