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
    window.roomifyMasterRooms = [];
    window.roomifyAreaSuggestions = [];

    window.roomifySearchQuery = "";

    function getBackendBaseUrl() {
        if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
            return 'http://' + window.location.hostname + ':8080';
        }
        return 'https://roomifytz-backend.fly.dev';
    }

    function resolveMediaUrl(url) {
        if (!url) return '';
        var clean = String(url).trim();
        if (clean.startsWith('http://') || clean.startsWith('https://') || clean.startsWith('data:')) {
            return clean;
        }
        var mediaBase = getBackendBaseUrl();
        if (!clean.startsWith('/')) {
            clean = '/' + clean;
        }
        return mediaBase + clean;
    }

    function deriveAreaSuggestionsFromRooms() {
        if (!window.roomifyAllRooms || window.roomifyAllRooms.length === 0) return;
        var areas = new Set();
        window.roomifyAllRooms.forEach(function(room) {
            if (!room) return;
            var addr = room.address || room.city || room.areaName || room.location || "";
            if (addr) {
                var parts = addr.split(",");
                if (parts.length > 0 && parts[0].trim()) {
                    areas.add(parts[0].trim());
                }
            }
        });
        if (areas.size > 0) {
            window.roomifyAreaSuggestions = Array.from(areas);
            console.log("Roomify: Derived " + window.roomifyAreaSuggestions.length + " area suggestions from loaded rooms");
        }
    }

    function fetchAreaSuggestions() {
        var url = getBackendBaseUrl() + '/api/rooms/areas';
        fetch(url)
            .then(function(response) {
                if (!response.ok) {
                    throw new Error("HTTP " + response.status);
                }
                var contentType = response.headers.get("content-type");
                if (!contentType || !contentType.includes("application/json")) {
                    throw new Error("Response is not JSON");
                }
                return response.json();
            })
            .then(function(data) {
                if (Array.isArray(data) && data.length > 0) {
                    window.roomifyAreaSuggestions = data;
                    console.log("Roomify: Fetched " + window.roomifyAreaSuggestions.length + " area suggestions");
                } else {
                    deriveAreaSuggestionsFromRooms();
                }
            })
            .catch(function(err) {
                console.warn("Roomify: Could not fetch area suggestions from backend, deriving from rooms:", err.message);
                deriveAreaSuggestionsFromRooms();
            });
    }

    /*
     * ============================================================
     * ROOMIFY COLORS
     * ============================================================
     */

    var ROOMIFY_BLUE_START = "#1A237E";
    var ROOMIFY_BLUE_END = "#3949AB";
    var ROOMIFY_BLUE = "#3949AB";
    var ROOMIFY_SELECTED_ORANGE = "#D97706";
    var ROOMIFY_WHITE = "#FFFFFF";
    var ROOMIFY_WHITE_90 = "rgba(255,255,255,0.90)";
    var ROOMIFY_WHITE_80 = "rgba(255,255,255,0.80)";
    var ROOMIFY_WHITE_70 = "rgba(255,255,255,0.70)";
    var ROOMIFY_WHITE_55 = "rgba(255,255,255,0.55)";
    var ROOMIFY_WHITE_25 = "rgba(255,255,255,0.25)";
    var ROOMIFY_GREEN = "#16A34A";
    var ROOMIFY_YELLOW = "#D97706";
    var ROOMIFY_RED = "#C62828";
    var ROOMIFY_ORANGE = "#D97706";
    var ROOMIFY_VIEWED_GRAY = "#16A34A";
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
     * SHOW MAP & SIDEBAR VISIBILITY
     * ============================================================
     */

    window.roomifyIsMapPage = false;

    function updateSidebarVisibility() {
        var sidebar = document.getElementById("roomify-sidebar");
        var backdrop = document.getElementById("roomify-sidebar-backdrop");
        var menuControl = document.getElementById("roomify-menu-button");
        var searchControl = document.getElementById("roomify-search-control");
        var searchCount = document.getElementById("roomify-search-count");
        var topRightControls = document.getElementById("roomify-top-right-controls");
        var isSmall = window.innerWidth < 900;

        if (!window.roomifyIsMapPage) {
            if (sidebar) {
                sidebar.classList.remove("map-active");
                sidebar.classList.remove("open");
                sidebar.style.display = "none";
                sidebar.style.pointerEvents = "none";
            }
            if (backdrop) {
                backdrop.classList.remove("open");
                backdrop.style.display = "none";
                backdrop.style.pointerEvents = "none";
            }
            if (menuControl) menuControl.style.display = "none";
            if (searchControl) searchControl.style.display = "none";
            if (searchCount) searchCount.style.display = "none";
            if (topRightControls) topRightControls.style.display = "none";
            return;
        }

        // On Map Page
        if (sidebar) {
            sidebar.classList.add("map-active");
            if (isSmall) {
                if (sidebar.classList.contains("open")) {
                    sidebar.style.display = "flex";
                    sidebar.style.pointerEvents = "auto";
                } else {
                    sidebar.style.display = "none";
                    sidebar.style.pointerEvents = "none";
                }
            } else {
                sidebar.classList.remove("open");
                sidebar.style.display = "flex";
                sidebar.style.pointerEvents = "auto";
            }
        }

        if (menuControl) menuControl.style.display = isSmall ? "flex" : "none";
        if (searchControl) searchControl.style.display = "flex";
        if (searchCount) searchCount.style.display = isSmall ? "none" : "block";
        if (topRightControls) topRightControls.style.display = "flex";
    }

    function registerMapResizeListenerOnce() {
        if (window.roomifyMapResizeListenerBound) return;
        window.roomifyMapResizeListenerBound = true;
        window.addEventListener('resize', function() {
            var isSmall = window.innerWidth < 900;
            var compose = document.getElementById("ComposeTarget");
            var map = document.getElementById("google-map-container");

            if (window.roomifyIsMapPage) {
                if (map && map.classList.contains("map-mode")) {
                    map.style.left = isSmall ? "0px" : "400px";
                    map.style.width = isSmall ? "100vw" : "calc(100vw - 400px)";
                }
            } else {
                if (compose) {
                    compose.style.left = "0px";
                    compose.style.width = "100vw";
                }
            }
            updateSidebarVisibility();
            window.roomifyResizeMap();
        });
    }

    window.roomifyShowMap = function () {
        console.log("[ROOMIFY MAP] SHOW MAP START (Native JS Mode - Compose Overlay Disabled)");
        window.roomifyIsMapPage = true;
        registerMapResizeListenerOnce();

        var map = document.getElementById("google-map-container");
        var compose = document.getElementById("ComposeTarget");
        var isSmallScreen = window.innerWidth < 900;

        // 1. Completely hide and disable ComposeTarget so WebGL never conflicts with Google Maps JS
        if (compose) {
            compose.classList.add("map-mode");
            compose.style.visibility = "hidden";
            compose.style.opacity = "0";
            compose.style.pointerEvents = "none";
            compose.style.zIndex = "-1";
        }

        // 2. Show Native Google Map container
        if (map) {
            map.classList.add("map-mode");
            map.style.display = "block";
            map.style.visibility = "visible";
            map.style.opacity = "1";
            map.style.pointerEvents = "auto";
            map.style.zIndex = "9999";
            map.style.left = isSmallScreen ? "0px" : "400px";
            map.style.width = isSmallScreen ? "100vw" : "calc(100vw - 400px)";
            setTimeout(function() {
                if (typeof showLocationConsentPrompt === "function") {
                    showLocationConsentPrompt();
                }
            }, 300);
        } else {
            console.error("[ROOMIFY MAP] map container NOT FOUND");
        }

        updateSidebarVisibility();

        // Ensure map instance exists if Google Maps SDK is already loaded
        if (!window.roomifyMap && window.google && window.google.maps && typeof window.roomifyCreateMap === "function") {
            window.roomifyCreateMap();
        }

        setTimeout(function () {
            if (window.roomifyMap && window.google && window.google.maps) {
                google.maps.event.trigger(window.roomifyMap, "resize");
            }
        }, 50);

        setTimeout(function () {
            if (window.roomifyMap && window.google && window.google.maps) {
                google.maps.event.trigger(window.roomifyMap, "resize");
            }
        }, 250);
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
        console.log("[ROOMIFY MAP] HIDE MAP (Restoring Compose Canvas)");
        window.roomifyIsMapPage = false;

        var map = document.getElementById("google-map-container");
        var compose = document.getElementById("ComposeTarget");

        if (map) {
            map.classList.remove("map-mode");
            map.style.display = "none";
            map.style.visibility = "hidden";
            map.style.opacity = "0";
            map.style.pointerEvents = "none";
            map.style.zIndex = "-1";
            console.log("[ROOMIFY MAP] map hidden");
        }

        if (compose) {
            compose.classList.remove("map-mode");
            compose.style.left = "0";
            compose.style.width = "100vw";
            compose.style.visibility = "visible";
            compose.style.opacity = "1";
            compose.style.pointerEvents = "auto";
            compose.style.zIndex = "10";
            compose.style.background = "#E8EEF7";
            compose.style.backgroundColor = "#E8EEF7";
            console.log("[ROOMIFY MAP] ComposeTarget restored to full width");
        }

        updateSidebarVisibility();
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
                background: linear-gradient(180deg, #F0F4FC 0%, #E8EEF7 100%) !important;
                border-right: 1px solid #D5E0F0 !important;
                z-index: 10000 !important;
                box-shadow: 10px 0 30px rgba(20,33,112,0.08);
                display: none;
                flex-direction: column;
                font-family: 'Inter', Arial, sans-serif;
                color: #0F172A;
                transform: none !important;
                transition: none !important;
                pointer-events: none;
            }

            .roomify-sidebar.map-active {
                display: flex !important;
                pointer-events: auto !important;
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
                flex: 1 1 auto;
                min-height: 0;
                padding: 30px;
                overflow-y: auto;
                -webkit-overflow-scrolling: touch;
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

            .roomify-chip .dot {
                width: 9px;
                height: 9px;
                border-radius: 50%;
                margin-right: 10px;
            }

            /* Professional Button Hover and Active/Click States */
            button,
            .roomify-sidebar-filter-btn,
            .roomify-location-btn,
            .roomify-footer-item,
            .roomify-popup-button,
            .roomify-menu-button,
            .roomify-status-control,
            .roomify-chip {
                transition: background-color 160ms ease, border-color 160ms ease, box-shadow 160ms ease, transform 160ms ease;
            }

            button:hover,
            .roomify-sidebar-filter-btn:hover,
            .roomify-location-btn:hover,
            .roomify-footer-item:hover,
            .roomify-popup-button:hover,
            .roomify-menu-button:hover,
            .roomify-status-control:hover,
            .roomify-chip:hover {
                transform: translateY(-1px);
                box-shadow: 0 3px 8px rgba(0,0,0,0.15);
            }

            button:active,
            .roomify-sidebar-filter-btn:active,
            .roomify-location-btn:active,
            .roomify-footer-item:active,
            .roomify-popup-button:active,
            .roomify-menu-button:active,
            .roomify-status-control:active,
            .roomify-chip:active {
                transform: translateY(0);
            }

            .roomify-sidebar-footer {
                flex-shrink: 0;
                padding: 20px 24px;
                border-top: 1px solid #F1F5F9;
                background: #ffffff;
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

            @media (max-width: 899px) {
                .roomify-sidebar {
                    position: fixed !important;
                    top: 0 !important;
                    left: -100% !important;
                    width: 320px !important;
                    max-width: 85vw !important;
                    height: 100vh !important;
                    height: 100dvh !important;
                    z-index: 99999 !important;
                    display: none;
                    flex-direction: column !important;
                    overflow: hidden !important;
                    transition: left 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
                    box-shadow: 4px 0 24px rgba(0,0,0,0.25) !important;
                    pointer-events: none;
                }
                .roomify-sidebar.map-active {
                    display: none;
                }
                .roomify-sidebar.map-active.open {
                    display: flex !important;
                    left: 0 !important;
                    pointer-events: auto !important;
                }
                .roomify-sidebar-header {
                    padding: 24px 20px 20px 20px !important;
                    border-radius: 0 0 24px 0 !important;
                    flex-shrink: 0 !important;
                }
                .roomify-sidebar-brand {
                    font-size: 24px !important;
                }
                .roomify-sidebar-tagline {
                    font-size: 13px !important;
                }
                .roomify-sidebar-body {
                    flex: 1 1 auto !important;
                    min-height: 0 !important;
                    padding: 16px !important;
                    overflow-y: auto !important;
                    -webkit-overflow-scrolling: touch !important;
                }
                .roomify-sidebar-footer {
                    flex-shrink: 0 !important;
                    padding: 16px 20px !important;
                    background: #ffffff !important;
                    border-top: 1px solid #E5E7EB !important;
                    box-shadow: 0 -2px 10px rgba(0,0,0,0.05) !important;
                }
                .roomify-footer-item {
                    font-size: 16px !important;
                }
                #google-map-container, #google-map-container.map-mode {
                    left: 0 !important;
                    width: 100vw !important;
                    margin-left: 0 !important;
                }
                #ComposeTarget, #ComposeTarget.map-mode {
                    left: 0 !important;
                    width: 100vw !important;
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
                font-family: system-ui, -apple-system, sans-serif;
                padding: 16px;
                width: 320px;
                max-width: 360px;
                background: #FFFFFF;
                border-radius: 18px;
                box-shadow: 0 10px 25px rgba(0,0,0,0.20);
                border: 1px solid #E2E8F0;
                color: #0F172A;
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
                background: #F1F5F9;
                border: 1px solid #E2E8F0;
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
                color: #142170;
                letter-spacing: 0.6px;
                text-transform: uppercase;
            }

            .roomify-popup-image-container {
                width: 100%;
                height: 150px;
                margin: 10px 0;
                border-radius: 12px;
                overflow: hidden;
                background: #F1F5F9;
                position: relative;
            }

            .roomify-popup-image {
                width: 100%;
                height: 150px;
                object-fit: cover;
                transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
            }

            .roomify-popup:hover .roomify-popup-image {
                transform: scale(1.03);
            }

            .roomify-popup-title {
                font-size: 16px;
                font-weight: 700;
                color: #0F172A;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }

            .roomify-popup-close {
                width: 32px;
                height: 32px;
                border: 0;
                border-radius: 50%;
                background: #F1F5F9;
                cursor: pointer;
                color: #334155;
                font-size: 16px;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-left: auto;
                flex-shrink: 0;
            }

            .roomify-popup-close:hover {
                background: #E2E8F0;
            }

            .roomify-popup-location {
                display: flex;
                align-items: center;
                padding: 4px 0;
                margin: 4px 0 8px 0;
            }

            .roomify-popup-location-icon {
                font-size: 14px;
                margin-right: 6px;
                color: #64748B;
            }

            .roomify-popup-location-text {
                font-size: 12px;
                font-weight: 500;
                color: #64748B;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }

            .roomify-popup-price-row {
                display: flex;
                align-items: center;
                justify-content: space-between;
                margin: 6px 0 10px 0;
            }

            .roomify-popup-price-label {
                font-size: 10px;
                font-weight: 600;
                color: #64748B;
                letter-spacing: 0.6px;
                text-transform: uppercase;
            }

            .roomify-popup-price {
                font-size: 18px;
                font-weight: 700;
                color: #0F172A;
            }

            .roomify-popup-status {
                display: inline-flex;
                align-items: center;
                padding: 4px 10px;
                border-radius: 50px;
                font-size: 11px;
                font-weight: 600;
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
                padding: 5px 10px;
                border-radius: 8px;
                background: #F1F5F9;
                border: 1px solid #E2E8F0;
                font-size: 12px;
                font-weight: 500;
                color: #334155;
            }

            .roomify-popup-feature-icon {
                font-size: 13px;
                margin-right: 4px;
                color: #64748B;
            }

            .roomify-popup-button {
                display: block;
                width: 100%;
                height: 44px;
                padding: 0 12px;
                border: 0;
                border-radius: 12px;
                background: #142170;
                color: #FFFFFF;
                font-family: inherit;
                font-size: 13px;
                font-weight: 700;
                cursor: pointer;
                transition: background 0.2s ease;
                margin-top: 6px;
            }

            .roomify-popup-button:hover {
                background: #0d164a;
            }

            .roomify-units-chips {
                display: flex;
                gap: 6px;
                overflow-x: auto;
                padding: 4px 0 8px 0;
                margin: 4px 0 8px 0;
                max-width: 100%;
                scrollbar-width: thin;
            }

            .roomify-unit-chip {
                flex: 0 0 auto;
                padding: 5px 10px;
                border-radius: 9px;
                background: rgba(255,255,255,0.15);
                border: 1px solid rgba(255,255,255,0.25);
                color: #ffffff;
                font-family: Arial, sans-serif;
                font-size: 11px;
                font-weight: 700;
                cursor: pointer;
                transition: all 0.15s ease;
            }

            .roomify-unit-chip:hover {
                background: rgba(255,255,255,0.3);
                transform: translateY(-1px);
            }

            .roomify-unit-chip.occupied {
                background: rgba(198,40,40,0.3);
                border-color: rgba(198,40,40,0.5);
            }


            /*
             * ====================================================
             * MOBILE
             * ====================================================
             */

            @media (max-width: 900px) {
                .roomify-search-control {
                    width: min(380px, calc(100vw - 110px));
                    height: 42px;
                    margin-top: 10px;
                    border-radius: 15px;
                }
                .roomify-menu-button {
                    width: 42px;
                    height: 42px;
                    margin-top: 10px;
                    margin-left: 10px;
                }
                .roomify-status-control {
                    height: 42px;
                    font-size: 13px;
                }
            }

            @media (max-width: 600px) {
                .roomify-search-control {
                    width: min(260px, calc(100vw - 75px));
                    height: 36px;
                    margin-top: 8px;
                    border-radius: 12px;
                }
                .roomify-search-icon {
                    width: 16px;
                    height: 16px;
                    margin-left: 10px;
                }
                .roomify-search-input {
                    font-size: 12px;
                    padding: 0 8px;
                }
                .roomify-search-clear {
                    width: 28px;
                    height: 28px;
                    font-size: 14px;
                }
                .roomify-menu-button {
                    width: 36px;
                    height: 36px;
                    margin-top: 8px;
                    margin-left: 6px;
                    border-radius: 10px;
                    font-size: 16px;
                }
                .roomify-status-control {
                    height: 36px;
                    font-size: 11px;
                    min-width: 80px;
                    padding: 0 6px;
                    border-radius: 10px;
                }
                .roomify-sidebar {
                    width: 300px;
                    max-width: 86vw;
                }
                .roomify-sidebar-header {
                    padding: 20px 16px 16px 16px !important;
                }
                .roomify-sidebar-body {
                    padding: 12px 14px !important;
                }
                .roomify-sidebar-footer {
                    padding: 14px 16px !important;
                }
                .roomify-footer-item {
                    font-size: 15px !important;
                }
                .roomify-popup {
                    min-width: 180px;
                    max-width: 220px;
                    max-height: calc(100vh - 80px);
                    overflow-y: auto;
                    padding: 10px;
                    border-radius: 16px;
                }
                .roomify-popup-image-container {
                    height: 100px;
                    margin: 6px 0;
                    border-radius: 12px;
                }
                .roomify-popup-title {
                    font-size: 14px;
                }
                .roomify-popup-price {
                    font-size: 15px;
                }
                .roomify-popup-location-text {
                    font-size: 11px;
                }
                .roomify-popup-button {
                    padding: 7px 10px;
                    font-size: 11px;
                    border-radius: 10px;
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
        if (document.getElementById("roomify-menu-button")) {
            return document.getElementById("roomify-menu-button");
        }

        var btn = document.createElement("div");
        btn.id = "roomify-menu-button";
        btn.className = "roomify-menu-button";
        btn.innerHTML = `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="12" x2="21" y2="12"></line><line x1="3" y1="6" x2="21" y2="6"></line><line x1="3" y1="18" x2="21" y2="18"></line></svg>`;
        btn.style.display = (window.innerWidth < 900) ? "flex" : "none";
        btn.onclick = function() {
            toggleSidebar();
        };

        return btn;
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
        // Map status dropdown control removed per requirements.
        return null;
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

    var ZOOM_THRESHOLD = 12;
    var INITIAL_MAP_ZOOM = 13;
    window.roomifyZoomListenerRegistered = false;
    window.roomifyLocationPromptShown = false;

    function normalizePropertyStatus(status) {
        return String(status ?? "").trim().toUpperCase();
    }

    function showLocationConsentPrompt() {
        if (window.roomifyLocationPromptShown) return;
        window.roomifyLocationPromptShown = true;

        var container = document.getElementById("google-map-container");
        if (!container) return;

        var overlay = document.createElement("div");
        overlay.id = "roomify-location-consent-overlay";
        overlay.style.cssText = `
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.4);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10005;
            font-family: Arial, sans-serif;
        `;

        var card = document.createElement("div");
        card.style.cssText = `
            background: #ffffff;
            border-radius: 20px;
            padding: 28px;
            max-width: 380px;
            width: 90%;
            box-shadow: 0 10px 30px rgba(0,0,0,0.25);
            text-align: left;
            border: 1px solid #E0E0E0;
        `;

        card.innerHTML = `
            <div style="font-size: 20px; font-weight: 800; color: #1A237E; margin-bottom: 10px;">Use Your Current Location?</div>
            <div style="font-size: 14px; color: #4B5563; line-height: 1.5; margin-bottom: 24px;">Allow ROOMIFY to use your current location to show nearby properties and improve your map experience.</div>
            <div style="display: flex; gap: 12px; justify-content: flex-end;">
                <button id="roomify-location-not-now" style="padding: 10px 18px; border: 1px solid #D1D5DB; background: #ffffff; color: #374151; border-radius: 12px; font-weight: 700; cursor: pointer; font-size: 14px; transition: all 160ms ease;">Not Now</button>
                <button id="roomify-location-use" style="padding: 10px 18px; border: 0; background: #1A237E; color: #ffffff; border-radius: 12px; font-weight: 700; cursor: pointer; font-size: 14px; transition: all 160ms ease;">Use My Location</button>
            </div>
            <div id="roomify-location-message" style="margin-top: 12px; font-size: 12px; color: #6B7280; display: none;"></div>
        `;

        overlay.appendChild(card);
        container.appendChild(overlay);

        var notNowBtn = card.querySelector("#roomify-location-not-now");
        var useBtn = card.querySelector("#roomify-location-use");
        var msgDiv = card.querySelector("#roomify-location-message");

        notNowBtn.onclick = function() {
            overlay.remove();
        };

        useBtn.onclick = function() {
            if (!navigator.geolocation) {
                msgDiv.textContent = "We couldn't determine your current location. Please try again or continue using the map.";
                msgDiv.style.display = "block";
                return;
            }

            msgDiv.textContent = "Locating...";
            msgDiv.style.display = "block";
            useBtn.disabled = true;

            navigator.geolocation.getCurrentPosition(
                function(position) {
                    overlay.remove();
                    var lat = position.coords.latitude;
                    var lng = position.coords.longitude;
                    if (window.roomifyMap) {
                        window.roomifyMap.setCenter({ lat: lat, lng: lng });
                        window.roomifyMap.setZoom(14);
                    }
                    if (typeof window.roomifyMoveToRoom === "function") {
                        window.roomifyMoveToRoom(lat, lng);
                    }
                },
                function(error) {
                    useBtn.disabled = false;
                    if (error.code === error.PERMISSION_DENIED) {
                        msgDiv.textContent = "Location access was not enabled. You can continue using the map without it.";
                    } else {
                        msgDiv.textContent = "We couldn't determine your current location. Please try again or continue using the map.";
                    }
                },
                { timeout: 10000, maximumAge: 60000 }
            );
        };
    }

    function applyMarkerVisibility() {
        if (!Array.isArray(window.roomifyMarkers)) return;

        var filter = normalizePropertyStatus(window.roomifyCurrentStatusFilter || "ALL");

        window.roomifyMarkers.forEach(function(entry) {
            if (!entry || !entry.marker) return;

            var visible = true;
            if (entry.building) {
                if (filter === "ALL" || filter === "") {
                    visible = true;
                } else {
                    visible = entry.building.rooms.some(function(r) {
                        return normalizePropertyStatus(r.status) === filter;
                    });
                }
            } else if (entry.room) {
                var status = normalizePropertyStatus(entry.room.status);
                if (filter === "ALL" || filter === "") {
                    visible = true;
                } else if (filter === "AVAILABLE") {
                    visible = (status === "AVAILABLE");
                } else if (filter === "PENDING") {
                    visible = (status === "PENDING");
                } else if (filter === "RENTED") {
                    visible = (status === "RENTED");
                } else {
                    visible = (status === filter);
                }
            }

            entry.marker.setVisible(visible);

            if (!visible && window.roomifySelectedRoomId && entry.room && String(entry.room.id) === String(window.roomifySelectedRoomId)) {
                window.roomifySelectedRoomId = null;
                if (window.roomifyInfoOverlay) {
                    window.roomifyInfoOverlay.hide();
                }
            }
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
                <div class="roomify-sidebar-close" onclick="window.roomifyCloseSidebar()">✕</div>
            </div>

            <div class="roomify-sidebar-body" style="padding: 20px;">
                <!-- 1. Nearby Me Button at the Top with Visible Border -->
                <div class="roomify-sidebar-section" style="margin-bottom: 20px;">
                    <div class="roomify-location-btn" id="use-my-location" style="border: 1.5px solid #1A237E; border-radius: 12px; background: #ffffff; padding: 12px; display: flex; align-items: center; justify-content: center; gap: 8px; cursor: pointer; color: #1A237E; font-weight: 700;">
                        <span>Nearby me</span>
                    </div>
                </div>

                <!-- 2. Preferences Card with Visible Outer Border -->
                <div class="roomify-sidebar-section" style="margin-bottom: 20px;">
                    <div style="border: 1px solid #CBD5E1; border-radius: 20px; padding: 20px; background: #ffffff;">
                        <div style="font-size: 16px; font-weight: bold; color: #111827; margin-bottom: 16px;">Preferences</div>

                        <!-- Location Field (Placeholder "Location", no separate heading above) -->
                        <div style="margin-bottom: 16px;">
                            <input type="text" id="sidebar-search-input" placeholder="Location" style="width: 100%; height: 48px; border: 1px solid #CBD5E1; border-radius: 14px; padding: 0 14px; outline: none; font-size: 14px; background: #ffffff;">
                        </div>

                        <!-- What are you looking for? -->
                        <div style="font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 8px;">What are you looking for?</div>
                        <div class="roomify-chip-group" id="type-chips" style="display: flex; gap: 8px; overflow-x: auto; margin-bottom: 16px; padding-bottom: 4px;">
                            <div class="roomify-chip active" data-type="ALL" style="padding: 6px 12px; border-radius: 8px; background: #F1F5F9; cursor: pointer; font-size: 13px; white-space: nowrap;">All</div>
                            <div class="roomify-chip" data-type="Room" style="padding: 6px 12px; border-radius: 8px; background: #F1F5F9; cursor: pointer; font-size: 13px; white-space: nowrap;">Room</div>
                            <div class="roomify-chip" data-type="Apartment" style="padding: 6px 12px; border-radius: 8px; background: #F1F5F9; cursor: pointer; font-size: 13px; white-space: nowrap;">Apartment</div>
                            <div class="roomify-chip" data-type="Studio" style="padding: 6px 12px; border-radius: 8px; background: #F1F5F9; cursor: pointer; font-size: 13px; white-space: nowrap;">Studio</div>
                            <div class="roomify-chip" data-type="House" style="padding: 6px 12px; border-radius: 8px; background: #F1F5F9; cursor: pointer; font-size: 13px; white-space: nowrap;">House</div>
                            <div class="roomify-chip" data-type="Office" style="padding: 6px 12px; border-radius: 8px; background: #F1F5F9; cursor: pointer; font-size: 13px; white-space: nowrap;">Office</div>
                        </div>

                        <!-- Max Budget Field (Placeholder "Max budget", no separate heading above) -->
                        <div style="margin-bottom: 16px;">
                            <input type="text" id="sidebar-budget-input" placeholder="Max budget" style="width: 100%; height: 48px; border: 1px solid #CBD5E1; border-radius: 14px; padding: 0 14px; outline: none; font-size: 14px; background: #ffffff;">
                        </div>

                        <!-- Status Section inside Preferences card -->
                        <div style="font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 8px;">Status</div>
                        <div class="roomify-chip-group" id="status-chips" style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 20px;">
                            <div class="roomify-chip" data-status="ALL" style="padding: 6px 12px; border-radius: 8px; background: #F1F5F9; cursor: pointer; font-size: 13px;">All</div>
                            <div class="roomify-chip active" data-status="AVAILABLE" style="padding: 6px 12px; border-radius: 8px; background: #F1F5F9; cursor: pointer; font-size: 13px;">Available</div>
                            <div class="roomify-chip" data-status="PENDING" style="padding: 6px 12px; border-radius: 8px; background: #F1F5F9; cursor: pointer; font-size: 13px;">Pending</div>
                            <div class="roomify-chip" data-status="RENTED" style="padding: 6px 12px; border-radius: 8px; background: #F1F5F9; cursor: pointer; font-size: 13px;">Rented</div>
                        </div>

                        <!-- APPLY FILTER & CLEAR FILTER Buttons -->
                        <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                            <div id="sidebar-filter-btn" style="flex: 1; min-width: 110px; display: flex; align-items: center; justify-content: center; background: #1A237E; color: white; border-radius: 14px; height: 48px; font-weight: 700; cursor: pointer; font-size: 13px; letter-spacing: 0.5px;">
                                Apply Filter
                            </div>
                            <div id="sidebar-clear-btn" onclick="window.roomifyClearSidebarFilters()" style="flex: 1; min-width: 110px; display: flex; align-items: center; justify-content: center; background: #ffffff; color: #1A237E; border: 1.5px solid #1A237E; border-radius: 14px; height: 48px; font-weight: 700; cursor: pointer; font-size: 13px; letter-spacing: 0.5px;">
                                Clear Filter
                            </div>
                        </div>

                        <!-- NO RESULTS CONTAINER -->
                        <div id="sidebar-no-results" style="display: none; padding: 16px; background: #FEF2F2; border: 1px solid #FCA5A5; border-radius: 14px; text-align: center; margin-top: 12px;">
                            <div style="font-size: 14px; font-weight: 700; color: #991B1B; margin-bottom: 4px;">No properties found</div>
                            <div style="font-size: 12px; color: #7F1D1D; margin-bottom: 12px; line-height: 1.4;">Try changing your location, property type, or budget filters.</div>
                            <div id="sidebar-clear-filters-btn" onclick="window.roomifyClearSidebarFilters()" style="display: inline-block; padding: 8px 16px; background: #DC2626; color: white; border-radius: 10px; font-size: 12px; font-weight: 700; cursor: pointer;">Clear Filters</div>
                        </div>
                    </div>
                </div>

                <!-- 3. Explore Furniture -->
                <div class="roomify-sidebar-section" style="margin-bottom: 20px;">
                    <div class="roomify-sidebar-label" style="font-size: 11px; font-weight: bold; color: #374151; letter-spacing: 1px; margin-bottom: 8px;">EXPLORE FURNITURE</div>
                    <div class="roomify-furniture-card" data-roomify-menu="furniture_choice" style="display: flex; align-items: center; gap: 14px; padding: 14px 18px; background: #E8EAF6; border-radius: 16px; border: 1px solid #C7D2FE; cursor: pointer;">
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

        // DO NOT filter while typing (No input event listeners on sidebar search or budget inputs)
        const quickSearchInput = document.getElementById("sidebar-search-input");

        const sidebarFilterBtn = document.getElementById("sidebar-filter-btn");
        if (sidebarFilterBtn) {
            sidebarFilterBtn.onclick = function() {
                var count = applySidebarFilters();
                var isSmall = window.innerWidth < 900;
                if (isSmall) {
                    if (count > 0) {
                        closeSidebar();
                    } else {
                        var sidebarEl = document.getElementById("roomify-sidebar");
                        if (sidebarEl) {
                            sidebarEl.style.display = "flex";
                            sidebarEl.style.pointerEvents = "auto";
                            sidebarEl.classList.add("open");
                        }
                    }
                } else {
                    var sidebarEl = document.getElementById("roomify-sidebar");
                    if (sidebarEl) {
                        sidebarEl.style.display = "flex";
                        sidebarEl.style.pointerEvents = "auto";
                    }
                }
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

        setupChips("type-chips", "data-type", (type) => {});
        setupChips("status-chips", "data-status", (status) => {});

        // Max Budget Comma Formatting
        const budgetInput = document.getElementById("sidebar-budget-input");
        if (budgetInput) {
            budgetInput.addEventListener("input", function(e) {
                let numStr = e.target.value.replace(/[^0-9]/g, "");
                if (numStr) {
                    let num = parseInt(numStr, 10);
                    e.target.value = num.toLocaleString();
                } else {
                    e.target.value = "";
                }
            });
        }

        window.roomifyClearSidebarFilters = function() {
            var locEl = document.getElementById("sidebar-search-input");
            if (locEl) locEl.value = "";

            var budgetEl = document.getElementById("sidebar-budget-input");
            if (budgetEl) budgetEl.value = "";

            var typeChips = document.querySelectorAll("#type-chips .roomify-chip");
            typeChips.forEach(function(c) {
                if (c.getAttribute("data-type") === "ALL") c.classList.add("active");
                else c.classList.remove("active");
            });

            var statusChips = document.querySelectorAll("#status-chips .roomify-chip");
            statusChips.forEach(function(c) {
                if (c.getAttribute("data-status") === "ALL") c.classList.add("active");
                else c.classList.remove("active");
            });

            var noResultsEl = document.getElementById("sidebar-no-results");
            if (noResultsEl) noResultsEl.style.display = "none";

            applySidebarFilters();

            if (window.roomifyMasterRooms && window.roomifyMasterRooms.length > 0) {
                window.roomifyFitMapToRooms(JSON.stringify(window.roomifyMasterRooms));
            }
        };

        window.roomifySyncSidebarState = function(area, maxPrice, status, propertyType) {
            var locEl = document.getElementById("sidebar-search-input");
            if (locEl && area !== undefined) locEl.value = area || "";

            var budgetEl = document.getElementById("sidebar-budget-input");
            if (budgetEl && maxPrice !== undefined) {
                budgetEl.value = (maxPrice && !isNaN(maxPrice)) ? Math.round(maxPrice).toLocaleString() : "";
            }

            if (propertyType !== undefined) {
                var typeChips = document.querySelectorAll("#type-chips .roomify-chip");
                var targetType = (propertyType || "ALL").toUpperCase();
                typeChips.forEach(function(c) {
                    var chipType = (c.getAttribute("data-type") || "ALL").toUpperCase();
                    if (chipType === targetType) c.classList.add("active");
                    else c.classList.remove("active");
                });
            }

            if (status !== undefined) {
                var statusChips = document.querySelectorAll("#status-chips .roomify-chip");
                var targetStatus = (status || "ALL").toUpperCase();
                statusChips.forEach(function(c) {
                    var chipStatus = (c.getAttribute("data-status") || "ALL").toUpperCase();
                    if (chipStatus === targetStatus) c.classList.add("active");
                    else c.classList.remove("active");
                });
            }
        };

        function applySidebarFilters() {
            const locEl = document.getElementById("sidebar-search-input");
            const locVal = locEl ? locEl.value.trim() : "";

            const maxBudgetEl = document.getElementById("sidebar-budget-input");
            const rawBudget = maxBudgetEl ? maxBudgetEl.value.trim() : "";
            var clean = rawBudget.toLowerCase().replace(/,/g, "").replace("tzs", "").trim();
            var parsedBudget = parseFloat(clean);
            if (isNaN(parsedBudget) || parsedBudget <= 0) parsedBudget = null;

            const statusChipsEl = document.getElementById("status-chips");
            const statusActiveEl = statusChipsEl ? statusChipsEl.querySelector(".active") : null;
            const status = statusActiveEl ? statusActiveEl.getAttribute("data-status") : "ALL";
            window.roomifyCurrentStatusFilter = status;

            const typeChipsEl = document.getElementById("type-chips");
            const typeActiveEl = typeChipsEl ? typeChipsEl.querySelector(".active") : null;
            const type = typeActiveEl ? typeActiveEl.getAttribute("data-type") : "ALL";

            var sourceRooms = (window.roomifyMasterRooms && window.roomifyMasterRooms.length > 0)
                ? window.roomifyMasterRooms
                : window.roomifyAllRooms;

            var matches = [];
            if (sourceRooms && sourceRooms.length > 0) {
                matches = sourceRooms.filter(function(room) {
                    if (!room) return false;
                    if (parsedBudget !== null && room.price > parsedBudget) return false;
                    if (type && type !== "ALL") {
                        var pType = String(room.propertyType || "").toLowerCase();
                        if (!pType.includes(type.toLowerCase())) return false;
                    }
                    var rStatus = normalizePropertyStatus(room.status);
                    var cStatus = normalizePropertyStatus(status);
                    if (cStatus !== "ALL" && cStatus !== "" && rStatus !== cStatus) return false;
                    if (locVal) {
                        var searchable = [room.title, room.address, room.city, room.areaName, room.location].filter(Boolean).join(" ").toLowerCase();
                        if (!searchable.includes(locVal.toLowerCase())) return false;
                    }
                    return true;
                });
            }

            updateVisibleMarkers(matches);
            applyMarkerVisibility();

            var noResultsEl = document.getElementById("sidebar-no-results");
            if (matches.length === 0) {
                if (noResultsEl) noResultsEl.style.display = "block";
            } else {
                if (noResultsEl) noResultsEl.style.display = "none";

                if (locVal || parsedBudget || (type && type !== "ALL") || (status && status !== "ALL")) {
                    if (window.roomifyMap) {
                        var validRooms = matches.filter(function(r) {
                            var lat = Number(r.latitude !== undefined ? r.latitude : r.lat);
                            var lng = Number(r.longitude !== undefined ? r.longitude : r.lng);
                            return isFinite(lat) && isFinite(lng) && lat !== 0 && lng !== 0;
                        });

                        if (validRooms.length === 1) {
                            var lat = Number(validRooms[0].latitude !== undefined ? validRooms[0].latitude : validRooms[0].lat);
                            var lng = Number(validRooms[0].longitude !== undefined ? validRooms[0].longitude : validRooms[0].lng);
                            window.roomifyMap.setCenter(new google.maps.LatLng(lat, lng));
                            window.roomifyMap.setZoom(16);
                        } else if (validRooms.length > 1) {
                            var bounds = new google.maps.LatLngBounds();
                            validRooms.forEach(function(r) {
                                var lat = Number(r.latitude !== undefined ? r.latitude : r.lat);
                                var lng = Number(r.longitude !== undefined ? r.longitude : r.lng);
                                bounds.extend(new google.maps.LatLng(lat, lng));
                            });
                            if (!bounds.isEmpty()) {
                                window.roomifyMap.fitBounds(bounds);
                            }
                        }
                    }
                }
            }

            var event = new CustomEvent("roomifySidebarFilter", {
                detail: JSON.stringify({
                    area: locVal || null,
                    maxPrice: parsedBudget,
                    status: (status && status !== "ALL") ? status : null,
                    propertyType: (type && type !== "ALL") ? type : null
                })
            });
            document.dispatchEvent(event);

            return matches.length;
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
        updateSidebarVisibility();
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
        if (!window.roomifyIsMapPage) return;

        var sidebar = document.getElementById("roomify-sidebar");
        var backdrop = document.getElementById("roomify-sidebar-backdrop");

        if (!sidebar) {
            return;
        }

        if (sidebar.classList.contains("open")) {
            closeSidebar();
        } else {
            sidebar.classList.add("map-active");
            sidebar.classList.add("open");

            if (backdrop) {
                backdrop.style.display = "block";
                backdrop.style.pointerEvents = "auto";
                backdrop.classList.add("open");
            } else {
                createBackdrop();
            }
            updateSidebarVisibility();
        }
    }


    function closeSidebar() {
        var sidebar = document.getElementById("roomify-sidebar");
        var backdrop = document.getElementById("roomify-sidebar-backdrop");

        if (sidebar) {
            sidebar.classList.remove("open");
        }

        if (backdrop) {
            backdrop.classList.remove("open");
            backdrop.style.display = "none";
            backdrop.style.pointerEvents = "none";
        }

        updateSidebarVisibility();
    }

    window.roomifyCloseSidebar = closeSidebar;

    function createBackdrop() {
        if (document.getElementById("roomify-sidebar-backdrop")) return;
        var backdrop = document.createElement("div");
        backdrop.id = "roomify-sidebar-backdrop";
        backdrop.style.cssText = "position:fixed; top:0; left:0; width:100vw; height:100vh; background:rgba(0,0,0,0.4); z-index:9998; display:block; pointer-events:auto;";
        backdrop.onclick = function() {
            closeSidebar();
        };
        document.body.appendChild(backdrop);
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

        var sidebar = document.getElementById("roomify-sidebar");
        if (sidebar) {
            sidebar.classList.remove("open");
        }
        var backdrop = document.getElementById("roomify-sidebar-backdrop");
        if (backdrop) {
            backdrop.classList.remove("open");
            backdrop.style.display = "none";
        }

        if (destination !== "map" && destination !== "explore") {
            window.roomifyHideMap();
        }

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
            !window.roomifyMenuControlInstalled
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

        var currentStatus = normalizePropertyStatus(window.roomifyCurrentStatusFilter || "ALL");

        var filtered = window.roomifyAllRooms.filter(function(room) {
            if (maxBudget !== null && room.price > maxBudget) {
                return false;
            }

            if (targetType) {
                var pType = String(room.propertyType || "").toLowerCase();
                if (!pType.includes(targetType)) return false;
            }

            var roomStatus = normalizePropertyStatus(room.status);
            if (currentStatus !== "ALL" && currentStatus !== "") {
                if (roomStatus !== currentStatus) {
                    return false;
                }
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

        if (window.roomifyMap) {
            var validRooms = filtered.filter(function(r) {
                var lat = Number(r.latitude !== undefined ? r.latitude : r.lat);
                var lng = Number(r.longitude !== undefined ? r.longitude : r.lng);
                return isFinite(lat) && isFinite(lng) && lat !== 0 && lng !== 0;
            });

            if (validRooms.length === 1) {
                var lat = Number(validRooms[0].latitude !== undefined ? validRooms[0].latitude : validRooms[0].lat);
                var lng = Number(validRooms[0].longitude !== undefined ? validRooms[0].longitude : validRooms[0].lng);
                window.roomifyMap.setCenter(new google.maps.LatLng(lat, lng));
                window.roomifyMap.setZoom(16);
            } else if (validRooms.length > 1) {
                var bounds = new google.maps.LatLngBounds();
                validRooms.forEach(function(r) {
                    var lat = Number(r.latitude !== undefined ? r.latitude : r.lat);
                    var lng = Number(r.longitude !== undefined ? r.longitude : r.lng);
                    bounds.extend(new google.maps.LatLng(lat, lng));
                });
                if (!bounds.isEmpty()) {
                    window.roomifyMap.fitBounds(bounds);
                }
            }
        }
    }


    /*
     * ============================================================
     * GROUP ROOMS FOR MAP
     * ============================================================
     */

    function groupRoomsForMap(rooms) {
        if (!Array.isArray(rooms) || !rooms.length) return [];

        var buildingMap = new Map();

        rooms.forEach(function(room) {
            if (!room) return;

            var lat = Number(room.latitude !== undefined ? room.latitude : room.lat);
            var lng = Number(room.longitude !== undefined ? room.longitude : room.lng);
            if (!isFinite(lat) || !isFinite(lng) || lat === 0 || lng === 0) return;

            var roundedLat = Math.round(lat * 10000) / 10000;
            var roundedLng = Math.round(lng * 10000) / 10000;

            var buildingKey;
            if (room.propertyId !== undefined && room.propertyId !== null && room.propertyId !== "") {
                buildingKey = "prop_" + room.propertyId;
            } else {
                buildingKey = "coord_" + roundedLat + "_" + roundedLng;
            }

            if (!buildingMap.has(buildingKey)) {
                buildingMap.set(buildingKey, []);
            }
            buildingMap.get(buildingKey).push(room);
        });

        var items = [];
        buildingMap.forEach(function(groupRooms, key) {
            if (groupRooms.length > 1 || (groupRooms[0] && (groupRooms[0].propertyType === 'BUILDING' || groupRooms[0].propertyId))) {
                var first = groupRooms[0];
                var minPrice = Infinity;
                var maxPrice = 0;
                var availableCount = 0;

                groupRooms.forEach(function(r) {
                    var p = Number(r.price || 0);
                    if (p > 0) {
                        if (p < minPrice) minPrice = p;
                        if (p > maxPrice) maxPrice = p;
                    }
                    var status = String(r.status || "AVAILABLE").toUpperCase();
                    if (status === "AVAILABLE") {
                        availableCount++;
                    }
                });

                if (minPrice === Infinity) minPrice = 0;

                items.push({
                    isBuilding: true,
                    buildingKey: key,
                    title: first.title || first.address || "Multi-Unit Building",
                    address: first.address || "Location specified",
                    latitude: Number(first.latitude !== undefined ? first.latitude : first.lat),
                    longitude: Number(first.longitude !== undefined ? first.longitude : first.lng),
                    totalRooms: groupRooms.length,
                    availableRooms: availableCount,
                    minPrice: minPrice,
                    maxPrice: maxPrice,
                    rooms: groupRooms
                });
            } else {
                items.push({
                    isBuilding: false,
                    room: groupRooms[0]
                });
            }
        });

        return items;
    }

    /*
     * ============================================================
     * UPDATE VISIBLE MARKERS
     * ============================================================
     */

    function updateVisibleMarkers(rooms) {
        if (!window.roomifyMap) return;

        if (Array.isArray(window.roomifyMarkers)) {
            window.roomifyMarkers.forEach(function (entry) {
                if (entry && entry.marker) {
                    entry.marker.setMap(null);
                }
            });
        }
        window.roomifyMarkers = [];

        var items = groupRoomsForMap(rooms);

        items.forEach(function(item) {
            if (item.isBuilding) {
                createBuildingMarker(item);
            } else {
                createRoomMarker(item.room);
            }
        });

        applyMarkerVisibility();
    }

    function createBuildingMarker(building) {
        var latitude = Number(building.latitude);
        var longitude = Number(building.longitude);

        if (!isFinite(latitude) || !isFinite(longitude) || latitude === 0 || longitude === 0) {
            return;
        }

        var position = { lat: latitude, lng: longitude };

        var selected = building.rooms.some(function(r) {
            return String(r.id) === String(window.roomifySelectedRoomId);
        });

        var marker = new google.maps.Marker({
            position: position,
            map: window.roomifyMap,
            title: building.title,
            icon: createBuildingPriceIcon(
                building.minPrice,
                building.totalRooms,
                building.availableRooms,
                selected,
                building.rooms.some(r => window.roomifyViewedRoomIds.has(Number(r.id))),
                building.rooms.some(r => window.roomifySavedRoomIds.has(Number(r.id)))
            ),
            optimized: true,
            animation: google.maps.Animation.DROP
        });

        marker.addListener("mouseover", function () {
            var entry = window.roomifyMarkers.find(m => m.marker === marker);
            if (entry) entry.hovered = true;
            refreshMarkerIcons();

            showBuildingInfo(marker, building);
        });

        marker.addListener("mouseout", function () {
            var entry = window.roomifyMarkers.find(m => m.marker === marker);
            if (entry) entry.hovered = false;
            refreshMarkerIcons();
        });

        marker.addListener("click", function () {
            var primaryRoom = building.rooms.find(r => String(r.status || "").toUpperCase() === "AVAILABLE") || building.rooms[0];
            if (primaryRoom) {
                window.roomifySelectedRoomId = String(primaryRoom.id);
                window.roomifyViewedRoomIds.add(Number(primaryRoom.id));
            }
            window.roomifyPopupPinned = true;
            refreshMarkerIcons();
            showBuildingInfo(marker, building);

            if (primaryRoom) {
                document.dispatchEvent(new CustomEvent("roomMarkerClicked", { detail: String(primaryRoom.id) }));
            }
        });

        window.roomifyMarkers.push({
            marker: marker,
            building: building,
            room: building.rooms[0]
        });
    }

    function showBuildingInfo(marker, building) {
        if (!window.roomifyInfoOverlay) {
            createRoomInfoOverlay();
        }
        if (!window.roomifyInfoOverlay) return;

        var content = buildBuildingPopupContent(building);
        window.roomifyInfoOverlay.setContent(content, String(building.rooms[0]?.id || "bld"));

        var position = marker.getPosition();
        if (!position) return;

        window.roomifyInfoOverlay.setPosition(position);
        window.roomifyInfoOverlay.show();

        var overlayDiv = window.roomifyInfoOverlay.div;
        if (overlayDiv) {
            overlayDiv.onmouseleave = null;
            overlayDiv.onmouseleave = function () {
                if (!window.roomifyPopupPinned) {
                    window.roomifyInfoOverlay.hide();
                }
            };
        }
    }

    function buildBuildingPopupContent(building) {
        var title = escapeHtml(building.title || "Multi-Unit Building");
        var address = escapeHtml(building.address || "Location specified");
        var total = building.totalRooms;
        var available = building.availableRooms;

        var priceRangeText = "";
        if (building.minPrice > 0) {
            if (building.minPrice === building.maxPrice || building.maxPrice <= 0) {
                priceRangeText = formatFullPrice(building.minPrice);
            } else {
                priceRangeText = formatCompactPrice(building.minPrice) + " – " + formatFullPrice(building.maxPrice);
            }
        } else {
            priceRangeText = "Price on request";
        }

        var chipsHtml = '<div class="roomify-units-chips">';
        building.rooms.forEach(function(unit) {
            var unitLabel = unit.unitNumber ? ("Unit " + unit.unitNumber) : ("Room " + unit.id);
            var isAvail = String(unit.status || "AVAILABLE").toUpperCase() === "AVAILABLE";
            var chipClass = isAvail ? "roomify-unit-chip" : "roomify-unit-chip occupied";
            var chipPrice = formatCompactPrice(unit.price);
            chipsHtml += '<button class="' + chipClass + '" onclick="event.stopPropagation(); window.roomifyOnViewDetailsClick(\'' + escapeHtml(unit.id) + '\', event);">' +
                escapeHtml(unitLabel) + ' • ' + escapeHtml(chipPrice) +
                '</button>';
        });
        chipsHtml += '</div>';

        var primaryRoomId = escapeHtml(building.rooms[0]?.id || "");

        return (
            '<div class="roomify-popup-container-wrapper" onclick="event.stopPropagation();" onmousedown="event.stopPropagation();" onmouseup="event.stopPropagation();">' +
            '<div class="roomify-popup">' +
            '<div class="roomify-popup-header">' +
            '<div style="flex:1;min-width:0;">' +
            '<div class="roomify-popup-property-type">Building / Multi-Unit</div>' +
            '<div class="roomify-popup-title" title="' + title + '">' + title + '</div>' +
            '</div>' +
            '<button class="roomify-popup-close" data-roomify-close-popup="true">✕</button>' +
            '</div>' +

            '<div class="roomify-popup-location">' +
            '<span class="roomify-popup-location-text" title="' + address + '">' + address + '</span>' +
            '</div>' +

            '<div class="roomify-popup-price-row">' +
            '<div>' +
            '<div class="roomify-popup-price-label">RENT RANGE</div>' +
            '<div class="roomify-popup-price">' + priceRangeText + '</div>' +
            '</div>' +
            '<div class="roomify-popup-status">' +
            '<span class="roomify-popup-status-dot" style="background:' + (available > 0 ? '#4CAF50' : '#C62828') + ';"></span>' +
            available + ' / ' + total + ' Available' +
            '</div>' +
            '</div>' +

            '<div class="roomify-popup-price-label" style="margin-top:6px;">SELECTABLE UNITS</div>' +
            chipsHtml +

            '<div style="display:flex;gap:8px;margin-top:6px;">' +
                '<button type="button" style="flex:1;height:36px;border-radius:10px;border:1px solid #C7D2FE;background:#EEF2FF;color:#142170;font-size:11px;font-weight:800;cursor:pointer;" onclick="event.stopPropagation(); window.roomifyStartRouteForRoomId(\'' + primaryRoomId + '\', \'inmap\');">' +
                    '🗺️ In-Map Route' +
                '</button>' +
                '<button type="button" style="flex:1;height:36px;border-radius:10px;border:1px solid #BBF7D0;background:#ECFDF5;color:#047857;font-size:11px;font-weight:800;cursor:pointer;" onclick="event.stopPropagation(); window.roomifyStartRouteForRoomId(\'' + primaryRoomId + '\', \'google\');">' +
                    '↗ Google Maps' +
                '</button>' +
            '</div>' +

            '<button class="roomify-popup-button" style="cursor:pointer;" onclick="window.roomifyOnViewDetailsClick(\'' + primaryRoomId + '\', event);">' +
            'View Units & Details' +
            '</button>' +

            '</div>' +
            '</div>'
        );
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

                if (!window.roomifyZoomListenerRegistered && window.roomifyMap) {
                    window.roomifyMap.addListener("zoom_changed", function() {
                        console.log("Roomify: zoom changed to " + window.roomifyMap.getZoom());
                        refreshMarkerIcons();
                    });
                    window.roomifyZoomListenerRegistered = true;
                }
                refreshMarkerIcons();

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

        var validRooms = [];
        rooms.forEach(function(room) {
            var lat = Number(room.latitude !== undefined ? room.latitude : room.lat);
            var lng = Number(room.longitude !== undefined ? room.longitude : room.lng);

            if (isFinite(lat) && isFinite(lng) && lat !== 0 && lng !== 0) {
                validRooms.push({ lat: lat, lng: lng });
            }
        });

        if (validRooms.length === 0) {
            console.warn("Roomify: No valid coordinates for fitBounds");
            return;
        }

        if (validRooms.length === 1) {
            var pos = validRooms[0];
            console.log("Roomify: Centering map to single property at " + pos.lat + ", " + pos.lng);
            window.roomifyMap.setCenter(new google.maps.LatLng(pos.lat, pos.lng));
            window.roomifyMap.setZoom(16);
            return;
        }

        var bounds = new google.maps.LatLngBounds();
        validRooms.forEach(function(pos) {
            bounds.extend(new google.maps.LatLng(pos.lat, pos.lng));
        });

        console.log("Roomify: Fitting map to " + validRooms.length + " markers");
        window.roomifyMap.fitBounds(bounds);

        var listener = google.maps.event.addListener(window.roomifyMap, "idle", function() {
            if (window.roomifyMap.getZoom() > 16) window.roomifyMap.setZoom(16);
            google.maps.event.removeListener(listener);
        });
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
            Math.round(value)
                .toLocaleString("en-US") +
            " TZS"
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

        return (
            Math.round(value)
                .toLocaleString("en-US") +
            " TZS"
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
                return "#16A34A";

            case "PENDING":
                return "#D97706";

            case "RENTED":
                return "#D97706";

            default:
                return "#16A34A";
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

            return 1.0;
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

    function createBuildingPriceIcon(
        minPrice,
        totalRooms,
        availableRooms,
        selected,
        isViewed,
        isSaved
    ) {
        var priceText = "From " + formatCompactPrice(minPrice);
        var roomsText = availableRooms + " / " + totalRooms + " rooms";
        var text = priceText + " • " + roomsText;

        var color = selected ? ROOMIFY_SELECTED_ORANGE : (isSaved ? ROOMIFY_SAVED_PINK : "#16A34A");
        if (availableRooms === 0) color = "#D97706";

        var scale = selected ? 1.35 : 1.0;
        var width = Math.max(170, (text.length * 7.5) + 44) * scale;
        var height = 55 * scale;

        var buildingVectorSvg = '<g transform="translate(18, 14)"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-2 10h-4v4h-2v-4H7v-2h4V9h2v4h4v2z" fill="#FFFFFF"/></g>';

        var svg =
            '<svg xmlns="http://www.w3.org/2000/svg" ' +
            'width="' + width + '" height="' + height + '" ' +
            'viewBox="0 0 ' + width + ' 55">' +
            '<defs>' +
            '<filter id="shadow_bld" x="-50%" y="-50%" width="200%" height="200%">' +
                '<feDropShadow dx="0" dy="1.5" stdDeviation="2.5" flood-opacity="0.38"/>' +
            '</filter>' +
            '</defs>' +
            '<g filter="url(#shadow_bld)">' +
                '<rect x="5" y="5" rx="18" ry="18" width="' + (width - 10) + '" height="38" fill="' + color + '" stroke="#FFFFFF" stroke-width="2"/>' +
                '<path d="M' + (width/2 - 7) + ' 43 L' + (width/2) + ' 52 L' + (width/2 + 7) + ' 43" fill="' + color + '"/>' +
                '<circle cx="28" cy="24" r="14" fill="rgba(255,255,255,0.2)"/>' +
                buildingVectorSvg +
                '<text x="' + ((width + 28)/2) + '" y="30" text-anchor="middle" font-family="Arial,sans-serif" font-size="12" font-weight="900" fill="#ffffff">' +
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

    function formatLocationDisplayJs(str, room) {
        if (str) {
            var clean = String(str).replace(/^Coordinates:/i, '').trim();
            var isCoords = /^-?\d+(\.\d+)?,[\s]*-?\d+(\.\d+)?$/.test(clean);
            if (!isCoords && clean.length > 0) {
                return clean;
            }
        }
        if (room && Array.isArray(room.neighbourhoodPlaces) && room.neighbourhoodPlaces.length > 0) {
            return room.neighbourhoodPlaces[0];
        }
        return "Dar es Salaam, Tanzania";
    }

    function buildPopupContent(room) {

        var title =
            escapeHtml(
                room.title || "Modern apartment"
            );

        var price =
            escapeHtml(
                formatFullPrice(
                    room.price
                )
            );

        var vStatus =
            String(
                room.verificationStatus || ""
            ).toUpperCase();

        var status =
            String(
                room.status ||
                "AVAILABLE"
            ).toUpperCase();

        var statusLabel = escapeHtml(statusText(status));
        var statusContainerBg = "#DCFCE7";
        var statusDotBg = "#16A34A";
        var statusTextColor = "#15803D";

        if (vStatus === "PENDING" || status === "PENDING") {
            statusLabel = "Pending Verification";
            statusContainerBg = "#FEF3C7";
            statusDotBg = "#D97706";
            statusTextColor = "#B45309";
        } else if (vStatus === "REJECTED" || status === "RENTED") {
            statusLabel = status === "RENTED" ? "Rented" : "Rejected";
            statusContainerBg = "#FEF2F2";
            statusDotBg = "#DC2626";
            statusTextColor = "#991B1B";
        }

        var address =
            escapeHtml(
                room.address || ""
            );

        var propertyType =
            escapeHtml(
                (room.propertyType || "APARTMENT").toUpperCase()
            );

        var locationDisplay = escapeHtml(formatLocationDisplayJs(address, room));

        var photos = [];
        if (Array.isArray(room.fullImageUrls) && room.fullImageUrls.length > 0) {
            photos = room.fullImageUrls.filter(function(u) { return u && String(u).trim().length > 0; });
        } else if (Array.isArray(room.images) && room.images.length > 0) {
            photos = room.images.filter(function(u) { return u && String(u).trim().length > 0; });
        } else if (room.image && String(room.image).trim().length > 0) {
            photos = [room.image];
        }

        var imageHtml = "";
        if (photos.length > 0) {
            var resolvedImg = resolveMediaUrl(photos[0]);
            var photoBadge = photos.length > 1 ? '<div style="position:absolute; bottom:8px; right:8px; background:rgba(15,23,42,0.8); color:#FFFFFF; padding:3px 8px; border-radius:12px; font-size:10px; font-weight:bold; z-index:2;">1 / ' + photos.length + '</div>' : '';
            imageHtml =
                '<div class="roomify-popup-image-container" style="position:relative; height:150px; border-radius:12px; overflow:hidden; margin:10px 0; background:#F1F5F9;">' +
                photoBadge +
                '<img src="' + escapeHtml(resolvedImg) + '" class="roomify-popup-image" style="width:100%; height:150px; object-fit:cover; border-radius:12px;" onerror="this.onerror=null; this.style.display=\'none\'; this.nextElementSibling.style.display=\'flex\';" />' +
                '<div style="display:none; align-items:center; justify-content:center; width:100%; height:100%; color:#94A3B8; font-size:32px;">📷</div>' +
                '</div>';
        } else {
            imageHtml =
                '<div class="roomify-popup-image-container" style="position:relative; height:150px; border-radius:12px; overflow:hidden; margin:10px 0; background:#F1F5F9; display:flex; align-items:center; justify-content:center;">' +
                '<div style="display:flex; align-items:center; justify-content:center; width:100%; height:100%; color:#94A3B8; font-size:32px;">📷</div>' +
                '</div>';
        }

        var featuresHtml = "";
        var features = [];

        if (Number(room.roomsCount) > 0) {
            features.push(Number(room.roomsCount) + " Beds");
        }
        if (Number(room.bathroomsCount) > 0) {
            features.push(Number(room.bathroomsCount) + " Baths");
        }
        if (Number(room.area) > 0) {
            features.push(Math.round(Number(room.area)) + " m²");
        }

        if (features.length > 0) {
            featuresHtml = '<div class="roomify-popup-features">';
            features.forEach(function (feature) {
                featuresHtml +=
                    '<span class="roomify-popup-feature">' +
                    feature +
                    '</span>';
            });
            featuresHtml += '</div>';
        }

        var buttonText = "View Property Details";
        var buttonStyle = 'style="cursor:pointer;"';
        var safeRoomId = escapeHtml(room.id);

        return (
            '<div class="roomify-popup-container-wrapper" ' +
            'onclick="event.stopPropagation();" ' +
            'onmousedown="event.stopPropagation();" ' +
            'onmouseup="event.stopPropagation();">' +

            '<div class="roomify-popup">' +

            '<div class="roomify-popup-header">' +

            '<div style="flex:1;min-width:0;">' +
            '<div class="roomify-popup-property-type">' +
            propertyType +
            '</div>' +
            '<div class="roomify-popup-title" title="' + title + '">' +
            title +
            '</div>' +
            '</div>' +

            '<button class="roomify-popup-close" data-roomify-close-popup="true">' +
            '✕' +
            '</button>' +

            '</div>' +

            imageHtml +

            '<div class="roomify-popup-location">' +
            '<span class="roomify-popup-location-text" title="' + locationDisplay + '">' +
            locationDisplay +
            '</span>' +
            '</div>' +

            '<div class="roomify-popup-price-row">' +
            '<div>' +
            '<div class="roomify-popup-price-label">MONTHLY RENT</div>' +
            '<div class="roomify-popup-price">' + price + '</div>' +
            '</div>' +
            '<div class="roomify-popup-status" style="background:' + statusContainerBg + '; color:' + statusTextColor + ';">' +
            '<span class="roomify-popup-status-dot" style="background:' + statusDotBg + ';"></span>' +
            statusLabel +
            '</div>' +
            '</div>' +

            featuresHtml +

            '<div style="display:flex;gap:8px;margin-top:8px;">' +
                '<button type="button" style="flex:1;height:36px;border-radius:10px;border:1px solid #C7D2FE;background:#EEF2FF;color:#142170;font-size:11px;font-weight:800;cursor:pointer;" onclick="event.stopPropagation(); window.roomifyStartRouteForRoomId(\'' + safeRoomId + '\', \'inmap\');">' +
                    '🗺️ In-Map Route' +
                '</button>' +
                '<button type="button" style="flex:1;height:36px;border-radius:10px;border:1px solid #BBF7D0;background:#ECFDF5;color:#047857;font-size:11px;font-weight:800;cursor:pointer;" onclick="event.stopPropagation(); window.roomifyStartRouteForRoomId(\'' + safeRoomId + '\', \'google\');">' +
                    '↗ Google Maps' +
                '</button>' +
            '</div>' +

            '<button class="roomify-popup-button" ' + buttonStyle +
            ' onclick="window.roomifyOnViewDetailsClick(\'' + safeRoomId + '\', event);"' +
            ' data-roomify-view-details="' + safeRoomId + '">' +
            buttonText +
            '</button>' +

            '</div>' +

            '</div>'
        );
    }

    window.roomifyOnViewDetailsClick = function(roomId, event) {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
            if (typeof event.cancelBubble !== "undefined") event.cancelBubble = true;
        }

        if (!roomId) {
            console.warn("Roomify: View Details clicked without roomId");
            return;
        }

        console.log("Roomify: View Details clicked for room ID:", roomId);

        if (window.roomifyInfoOverlay) {
            window.roomifyInfoOverlay.hide();
        }
        window.roomifyPopupPinned = false;

        var customEvent = new CustomEvent("roomViewDetails", {
            detail: String(roomId)
        });
        document.dispatchEvent(customEvent);
    };


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
                            var roomId =
                                viewBtn.getAttribute(
                                    "data-roomify-view-details"
                                );
                            window.roomifyOnViewDetailsClick(roomId, event);
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
        var showClusters = zoom < ZOOM_THRESHOLD;
        var showDots = zoom < ZOOM_THRESHOLD;
        var showPills = zoom >= ZOOM_THRESHOLD;

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

                if (!window.roomifyMasterRooms || window.roomifyMasterRooms.length === 0 || rooms.length >= window.roomifyMasterRooms.length) {
                    window.roomifyMasterRooms = rooms;
                }

                window.roomifyAllRooms = rooms;


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

            window.clearRoute();

            var map =
                document.getElementById(
                    "google-map-container"
                );
            var isSmallScreen = window.innerWidth < 768;


            var menuControl = document.getElementById("roomify-menu-button");
        if (menuControl) {
            menuControl.style.display = isSmallScreen ? "flex" : "none";
        }

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


            window.roomifyMasterRooms = [];
            window.roomifyAllRooms = [];

            window.roomifySearchQuery =
                "";

            window.roomifyMap =
                null;


            console.log(
                "Roomify: map destroyed"
            );
        };


    /*
     * ============================================================
     * ROUTE DRAWING & TRACKING
     * ============================================================
     */

    window.roomifyDirectionsRenderer = null;
    window.roomifyRoutePolyline = null;
    window.roomifyRouteOutlinePolyline = null;
    window.roomifyOriginMarker = null;
    window.roomifyActiveRouteState = null;

    function haversineKm(lat1, lng1, lat2, lng2) {
        var R = 6371.0;
        var dLat = (lat2 - lat1) * Math.PI / 180;
        var dLng = (lng2 - lng1) * Math.PI / 180;
        var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    function formatDurationMinutes(mins) {
        var m = Math.max(1, Math.round(mins));
        if (m < 60) return m + " min";
        var h = Math.floor(m / 60);
        var rem = m % 60;
        return rem > 0 ? (h + " hr " + rem + " min") : (h + " hr");
    }

    window.roomifyOpenGoogleMapsRoute = function(originLat, originLng, destLat, destLng, travelMode) {
        var mode = (String(travelMode || "DRIVING").toUpperCase() === "WALKING") ? "walking" : "driving";
        var url = "https://www.google.com/maps/dir/?api=1";
        if (isFinite(Number(originLat)) && isFinite(Number(originLng)) && Number(originLat) !== 0 && Number(originLng) !== 0) {
            url += "&origin=" + encodeURIComponent(Number(originLat) + "," + Number(originLng));
        }
        url += "&destination=" + encodeURIComponent(Number(destLat) + "," + Number(destLng));
        url += "&travelmode=" + mode;
        window.open(url, "_blank", "noopener,noreferrer");
    };

    window.roomifyStartRouteForRoomId = function(roomId, modeChoice) {
        var targetRoom = null;
        if (Array.isArray(window.roomifyAllRooms)) {
            targetRoom = window.roomifyAllRooms.find(function(r) { return String(r.id) === String(roomId); });
        }
        if (!targetRoom && Array.isArray(window.roomifyMarkers)) {
            var entry = window.roomifyMarkers.find(function(m) { return m && m.room && String(m.room.id) === String(roomId); });
            if (entry) targetRoom = entry.room;
        }
        if (!targetRoom) return;

        var destLat = targetRoom.latitude !== undefined ? Number(targetRoom.latitude) : Number(targetRoom.lat);
        var destLng = targetRoom.longitude !== undefined ? Number(targetRoom.longitude) : Number(targetRoom.lng);
        if (!isFinite(destLat) || !isFinite(destLng) || destLat === 0 || destLng === 0) return;

        if (window.roomifyInfoOverlay) {
            window.roomifyInfoOverlay.hide();
        }
        window.roomifyPopupPinned = false;

        var resolveAndRun = function(oLat, oLng) {
            if (modeChoice === "google") {
                window.roomifyOpenGoogleMapsRoute(oLat, oLng, destLat, destLng, "DRIVING");
            } else {
                var cleanAddr = formatLocationDisplayJs(targetRoom.address, targetRoom);
                window.showRoute(oLat, oLng, destLat, destLng, targetRoom.title || "Selected Property", cleanAddr, "DRIVING");
            }
        };

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                function(pos) { resolveAndRun(pos.coords.latitude, pos.coords.longitude); },
                function() { resolveAndRun(-6.7924, 39.2083); },
                { enableHighAccuracy: true, timeout: 6000 }
            );
        } else {
            resolveAndRun(-6.7924, 39.2083);
        }
    };

    function renderNativeRouteBanner(state) {
        var existing = document.getElementById("roomify-native-route-card");
        if (existing) existing.remove();

        var mapContainer = document.getElementById("google-map-container") || document.body;
        var originLat = Number(state.originLat);
        var originLng = Number(state.originLng);
        var destLat = Number(state.destLat);
        var destLng = Number(state.destLng);
        var travelMode = state.travelMode || "DRIVING";

        var straightKm = haversineKm(originLat, originLng, destLat, destLng);
        var roadKm = state.distanceKm || (straightKm * 1.28);
        var distLabel = state.distanceText || (roadKm < 1 ? Math.round(roadKm * 1000) + " m" : roadKm.toFixed(1) + " km");
        var driveMins = state.driveMins || Math.max(1, Math.round((roadKm / 28.0) * 60));
        var walkMins = state.walkMins || Math.max(1, Math.round((roadKm / 4.8) * 60));
        var driveLabel = state.driveText || ("~" + formatDurationMinutes(driveMins));
        var walkLabel = state.walkText || ("~" + formatDurationMinutes(walkMins));

        var cleanSubtitle = state.destAddress || "";
        if (!cleanSubtitle || /^Coordinates:/i.test(cleanSubtitle.trim()) || /^-?\d+(\.\d+)?,[\s]*-?\d+(\.\d+)?$/.test(cleanSubtitle.trim())) {
            cleanSubtitle = "Dar es Salaam, Tanzania • " + destLat.toFixed(4) + ", " + destLng.toFixed(4);
        }

        var isDriving = travelMode === "DRIVING";
        var routeSourceBadge = state.routeSource === "ROAD" ? "ROAD ROUTE" : "LIVE ROUTE";

        var card = document.createElement("div");
        card.id = "roomify-native-route-card";
        card.style.cssText = "position:fixed;bottom:24px;right:24px;width:min(380px,calc(100vw - 32px));background:#FFFFFF;border-radius:20px;box-shadow:0 16px 38px rgba(20,33,112,0.26);border:1px solid #D5E0F0;z-index:10002;font-family:'Inter',Arial,sans-serif;overflow:hidden;";

        var header = document.createElement("div");
        header.style.cssText = "background:linear-gradient(90deg,#142170,#283593);padding:12px 16px;color:#FFFFFF;display:flex;align-items:center;justify-content:space-between;";
        header.innerHTML =
            '<div style="display:flex;align-items:center;gap:8px;">' +
                '<span style="width:8px;height:8px;border-radius:50%;background:#4ADE80;box-shadow:0 0 8px #4ADE80;display:inline-block;"></span>' +
                '<span style="font-size:11px;font-weight:800;letter-spacing:0.8px;">' + routeSourceBadge + ' • IN-MAP NAVIGATION</span>' +
            '</div>' +
            '<span style="font-size:12px;font-weight:800;background:rgba(255,255,255,0.18);padding:3px 10px;border-radius:12px;">' + distLabel + '</span>';

        var body = document.createElement("div");
        body.style.cssText = "padding:14px 16px;";

        var titleInfo = document.createElement("div");
        titleInfo.innerHTML =
            '<div style="font-size:15px;font-weight:800;color:#0F172A;margin-bottom:2px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">' + escapeHtml(state.destTitle || "Selected Property") + '</div>' +
            '<div style="font-size:12px;color:#64748B;margin-bottom:12px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">📍 ' + escapeHtml(cleanSubtitle) + '</div>';
        body.appendChild(titleInfo);

        // Interactive Travel Mode Switcher (Drive vs Walk inside map)
        var modeRow = document.createElement("div");
        modeRow.style.cssText = "display:flex;gap:8px;margin-bottom:10px;";

        var driveTab = document.createElement("button");
        driveTab.type = "button";
        driveTab.innerHTML = '🚗 Drive ' + driveLabel;
        driveTab.style.cssText = "flex:1;padding:9px 10px;border-radius:10px;font-size:12px;font-weight:800;cursor:pointer;border:" +
            (isDriving ? "1.5px solid #1D4ED8;background:#DBEAFE;color:#1E40AF;" : "1px solid #E2E8F0;background:#F8FAFC;color:#475569;");
        driveTab.onclick = function() {
            if (travelMode !== "DRIVING") {
                window.showRoute(originLat, originLng, destLat, destLng, state.destTitle, state.destAddress, "DRIVING");
            }
        };

        var walkTab = document.createElement("button");
        walkTab.type = "button";
        walkTab.innerHTML = '🚶 Walk ' + walkLabel;
        walkTab.style.cssText = "flex:1;padding:9px 10px;border-radius:10px;font-size:12px;font-weight:800;cursor:pointer;border:" +
            (!isDriving ? "1.5px solid #047857;background:#D1FAE5;color:#065F46;" : "1px solid #E2E8F0;background:#F8FAFC;color:#475569;");
        walkTab.onclick = function() {
            if (travelMode !== "WALKING") {
                window.showRoute(originLat, originLng, destLat, destLng, state.destTitle, state.destAddress, "WALKING");
            }
        };

        modeRow.appendChild(driveTab);
        modeRow.appendChild(walkTab);
        body.appendChild(modeRow);

        // Turn-by-turn steps preview if available
        if (Array.isArray(state.steps) && state.steps.length > 0) {
            var stepsBox = document.createElement("div");
            stepsBox.style.cssText = "max-height:88px;overflow-y:auto;background:#F8FAFC;border:1px solid #E2E8F0;border-radius:10px;padding:8px 10px;margin-bottom:10px;font-size:11px;color:#334155;";
            state.steps.slice(0, 4).forEach(function(st, idx) {
                var row = document.createElement("div");
                row.style.cssText = "display:flex;justify-content:space-between;gap:8px;padding:3px 0;" + (idx > 0 ? "border-top:1px dashed #E2E8F0;" : "");
                row.innerHTML = '<span style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;font-weight:600;">' + (idx + 1) + '. ' + escapeHtml(st.instruction) + '</span><span style="color:#64748B;flex-shrink:0;font-weight:700;">' + escapeHtml(st.distance || "") + '</span>';
                stepsBox.appendChild(row);
            });
            body.appendChild(stepsBox);
        }

        // Primary External Option: Open in Google Maps App / Web
        var gmapsBtn = document.createElement("button");
        gmapsBtn.type = "button";
        gmapsBtn.innerHTML = '↗ Navigate in Google Maps App';
        gmapsBtn.style.cssText = "width:100%;height:38px;border-radius:10px;border:1.5px solid #142170;background:#EEF2FF;color:#142170;font-weight:800;font-size:12px;cursor:pointer;margin-bottom:8px;display:flex;align-items:center;justify-content:center;gap:6px;";
        gmapsBtn.onclick = function() {
            window.roomifyOpenGoogleMapsRoute(originLat, originLng, destLat, destLng, travelMode);
        };
        body.appendChild(gmapsBtn);

        var actions = document.createElement("div");
        actions.style.cssText = "display:flex;gap:8px;";

        var recenterBtn = document.createElement("button");
        recenterBtn.type = "button";
        recenterBtn.textContent = "Recenter";
        recenterBtn.style.cssText = "flex:1;height:38px;border-radius:10px;border:none;background:#142170;color:#FFFFFF;font-weight:700;font-size:12px;cursor:pointer;";
        recenterBtn.onclick = function() {
            if (window.roomifyMap && window.google && window.google.maps) {
                var b = new google.maps.LatLngBounds();
                b.extend(new google.maps.LatLng(originLat, originLng));
                b.extend(new google.maps.LatLng(destLat, destLng));
                window.roomifyMap.fitBounds(b, { top: 80, right: 80, bottom: 260, left: 80 });
            }
        };

        var endBtn = document.createElement("button");
        endBtn.type = "button";
        endBtn.textContent = "End Route";
        endBtn.style.cssText = "flex:1;height:38px;border-radius:10px;border:none;background:#FEE2E2;color:#DC2626;font-weight:700;font-size:12px;cursor:pointer;";
        endBtn.onclick = function() {
            window.clearRoute();
            document.dispatchEvent(new CustomEvent("roomifyRouteCleared"));
        };

        actions.appendChild(recenterBtn);
        actions.appendChild(endBtn);
        body.appendChild(actions);
        card.appendChild(header);
        card.appendChild(body);
        mapContainer.appendChild(card);
    }

    function drawRoadCoordinatesOnMap(latLngArray, origin, dest, travelMode) {
        if (!window.roomifyMap || !window.google || !window.google.maps) return;
        var isWalking = travelMode === "WALKING";

        // Outer casing line for high contrast on map roads
        window.roomifyRouteOutlinePolyline = new google.maps.Polyline({
            path: latLngArray,
            geodesic: true,
            strokeColor: "#FFFFFF",
            strokeOpacity: 0.95,
            strokeWeight: 9,
            zIndex: 9990,
            map: window.roomifyMap
        });

        // Inner active road route line
        window.roomifyRoutePolyline = new google.maps.Polyline({
            path: latLngArray,
            geodesic: true,
            strokeColor: isWalking ? "#059669" : "#2563EB",
            strokeOpacity: 0.95,
            strokeWeight: 5,
            zIndex: 9991,
            map: window.roomifyMap
        });

        var bounds = new google.maps.LatLngBounds();
        latLngArray.forEach(function(pt) { bounds.extend(pt); });
        bounds.extend(origin);
        bounds.extend(dest);
        window.roomifyMap.fitBounds(bounds, { top: 80, right: 80, bottom: 260, left: 80 });
    }

    function fetchOsrmRoadRoute(originLat, originLng, destLat, destLng, travelMode, onSuccess, onFailure) {
        var profile = (travelMode === "WALKING") ? "foot" : "driving";
        var url = "https://router.project-osrm.org/route/v1/" + profile + "/" +
            Number(originLng) + "," + Number(originLat) + ";" +
            Number(destLng) + "," + Number(destLat) +
            "?overview=full&geometries=geojson&steps=true";

        fetch(url)
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (data && data.code === "Ok" && Array.isArray(data.routes) && data.routes.length > 0) {
                    var route = data.routes[0];
                    var coords = (route.geometry && Array.isArray(route.geometry.coordinates)) ? route.geometry.coordinates : [];
                    if (coords.length >= 2) {
                        var path = coords.map(function(c) {
                            return new google.maps.LatLng(Number(c[1]), Number(c[0]));
                        });
                        var distKm = (Number(route.distance) || 0) / 1000.0;
                        var durMins = Math.max(1, Math.round((Number(route.duration) || 0) / 60.0));
                        var steps = [];
                        if (Array.isArray(route.legs) && route.legs[0] && Array.isArray(route.legs[0].steps)) {
                            route.legs[0].steps.forEach(function(s) {
                                var name = s.name || "road";
                                var type = (s.maneuver && s.maneuver.type) ? s.maneuver.type : "Continue";
                                var mod = (s.maneuver && s.maneuver.modifier) ? (" " + s.maneuver.modifier) : "";
                                var stepDist = s.distance ? (s.distance < 1000 ? Math.round(s.distance) + " m" : (s.distance / 1000).toFixed(1) + " km") : "";
                                if (type !== "arrive") {
                                    steps.push({
                                        instruction: (type.charAt(0).toUpperCase() + type.slice(1) + mod + (name ? " onto " + name : "")),
                                        distance: stepDist
                                    });
                                }
                            });
                        }
                        onSuccess({
                            path: path,
                            distanceKm: distKm,
                            durationMins: durMins,
                            steps: steps
                        });
                        return;
                    }
                }
                onFailure();
            })
            .catch(function() {
                onFailure();
            });
    }

    window.showRoute = function(originLat, originLng, destLat, destLng, destTitle, destAddress, requestedMode) {
        if (!window.roomifyMap || !window.google || !window.google.maps) return;

        var travelMode = (String(requestedMode || "DRIVING").toUpperCase() === "WALKING") ? "WALKING" : "DRIVING";
        window.clearRoute();

        var origin = new google.maps.LatLng(Number(originLat), Number(originLng));
        var dest = new google.maps.LatLng(Number(destLat), Number(destLng));

        var routeState = {
            originLat: Number(originLat),
            originLng: Number(originLng),
            destLat: Number(destLat),
            destLng: Number(destLng),
            destTitle: destTitle || "Selected Property",
            destAddress: destAddress || "",
            travelMode: travelMode,
            routeSource: "LIVE",
            steps: []
        };
        window.roomifyActiveRouteState = routeState;
        renderNativeRouteBanner(routeState);

        var originSvg =
            '<svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 36 36">' +
            '<circle cx="18" cy="18" r="14" fill="#2563EB" stroke="#FFFFFF" stroke-width="3"/>' +
            '<circle cx="18" cy="18" r="6" fill="#FFFFFF"/>' +
            '</svg>';

        window.roomifyOriginMarker = new google.maps.Marker({
            position: origin,
            map: window.roomifyMap,
            title: "Your Current Location",
            icon: {
                url: "data:image/svg+xml;charset=UTF-8," + encodeURIComponent(originSvg),
                scaledSize: new google.maps.Size(36, 36),
                anchor: new google.maps.Point(18, 18)
            },
            zIndex: 99999
        });

        var useOsrmFallback = function() {
            fetchOsrmRoadRoute(
                originLat,
                originLng,
                destLat,
                destLng,
                travelMode,
                function(osrmResult) {
                    drawRoadCoordinatesOnMap(osrmResult.path, origin, dest, travelMode);
                    routeState.routeSource = "ROAD";
                    routeState.distanceKm = osrmResult.distanceKm;
                    routeState.distanceText = osrmResult.distanceKm < 1
                        ? Math.round(osrmResult.distanceKm * 1000) + " m"
                        : osrmResult.distanceKm.toFixed(1) + " km";
                    if (travelMode === "WALKING") {
                        routeState.walkMins = osrmResult.durationMins;
                        routeState.driveMins = Math.max(1, Math.round((osrmResult.distanceKm / 28.0) * 60));
                    } else {
                        routeState.driveMins = osrmResult.durationMins;
                        routeState.walkMins = Math.max(1, Math.round((osrmResult.distanceKm / 4.8) * 60));
                    }
                    routeState.steps = osrmResult.steps;
                    renderNativeRouteBanner(routeState);
                },
                function() {
                    drawRoadCoordinatesOnMap([origin, dest], origin, dest, travelMode);
                }
            );
        };

        if (typeof google.maps.DirectionsService === "function") {
            var directionsService = new google.maps.DirectionsService();
            var gMode = (travelMode === "WALKING")
                ? google.maps.TravelMode.WALKING
                : google.maps.TravelMode.DRIVING;

            directionsService.route({
                origin: origin,
                destination: dest,
                travelMode: gMode
            }, function(response, status) {
                if (status === "OK" && response && Array.isArray(response.routes) && response.routes.length > 0) {
                    window.roomifyDirectionsRenderer = new google.maps.DirectionsRenderer({
                        map: window.roomifyMap,
                        suppressMarkers: true,
                        polylineOptions: {
                            strokeColor: (travelMode === "WALKING") ? "#059669" : "#2563EB",
                            strokeWeight: 6,
                            strokeOpacity: 0.95
                        }
                    });
                    window.roomifyDirectionsRenderer.setDirections(response);

                    var leg = (response.routes[0].legs && response.routes[0].legs[0]) ? response.routes[0].legs[0] : null;
                    if (leg) {
                        routeState.routeSource = "ROAD";
                        if (leg.distance && leg.distance.text) {
                            routeState.distanceText = leg.distance.text;
                            routeState.distanceKm = (Number(leg.distance.value) || 0) / 1000.0;
                        }
                        if (leg.duration && leg.duration.text) {
                            if (travelMode === "WALKING") {
                                routeState.walkText = leg.duration.text;
                            } else {
                                routeState.driveText = leg.duration.text;
                            }
                        }
                        if (Array.isArray(leg.steps)) {
                            routeState.steps = leg.steps.map(function(s) {
                                var cleanInst = String(s.instructions || "").replace(/<[^>]*>/g, " ").replace(/\s+/g, " ").trim();
                                return {
                                    instruction: cleanInst,
                                    distance: (s.distance && s.distance.text) ? s.distance.text : ""
                                };
                            }).filter(function(s) { return s.instruction.length > 0; });
                        }
                        renderNativeRouteBanner(routeState);
                    }
                } else {
                    console.warn("Roomify: Google DirectionsService status=" + status + ", switching to OSRM real road route");
                    useOsrmFallback();
                }
            });
        } else {
            useOsrmFallback();
        }
    };

    window.clearRoute = function() {
        var existingCard = document.getElementById("roomify-native-route-card");
        if (existingCard) existingCard.remove();

        if (window.roomifyDirectionsRenderer) {
            window.roomifyDirectionsRenderer.setMap(null);
            window.roomifyDirectionsRenderer = null;
        }
        if (window.roomifyRoutePolyline) {
            window.roomifyRoutePolyline.setMap(null);
            window.roomifyRoutePolyline = null;
        }
        if (window.roomifyRouteOutlinePolyline) {
            window.roomifyRouteOutlinePolyline.setMap(null);
            window.roomifyRouteOutlinePolyline = null;
        }
        if (window.roomifyOriginMarker) {
            window.roomifyOriginMarker.setMap(null);
            window.roomifyOriginMarker = null;
        }
        window.roomifyActiveRouteState = null;
    };

    window.roomifyShowToast = function(message) {
        var existing = document.getElementById("roomify-native-toast");
        if (existing) existing.remove();
        if (!message) return;

        var toast = document.createElement("div");
        toast.id = "roomify-native-toast";
        toast.style.cssText = "position:fixed;top:84px;left:50%;transform:translateX(-50%);background:#1A237E;color:#FFFFFF;padding:10px 18px;border-radius:12px;font-family:'Inter',Arial,sans-serif;font-size:13px;font-weight:700;z-index:10005;box-shadow:0 8px 24px rgba(20,33,112,0.28);display:flex;align-items:center;gap:12px;";
        toast.innerHTML = '<span>' + message + '</span><span style="cursor:pointer;font-size:16px;line-height:1;">&times;</span>';
        toast.onclick = function() { toast.remove(); };
        document.body.appendChild(toast);
        setTimeout(function() {
            if (toast.parentNode) toast.remove();
        }, 4000);
    };


    console.log(
        "[ROOMIFY MAP] roomify-map.js loaded - v3.1 (Pure Native Google Maps Mode)"
    );

})();
