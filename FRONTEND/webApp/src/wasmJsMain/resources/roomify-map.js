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

    /*
     * Keep the complete room dataset.
     *
     * Search works against this list and then rebuilds the
     * displayed markers.
     */
    window.roomifyAllRooms = [];

    window.roomifySearchQuery = "";

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


    /*
     * ============================================================
     * SHOW MAP
     * ============================================================
     */

    window.roomifyShowMap = function () {

        var map =
            document.getElementById(
                "google-map-container"
            );

        if (map) {

            map.style.display = "block";
            map.style.visibility = "visible";
            map.style.opacity = "1";
            map.style.zIndex = "1";
            console.log("Roomify: Map layer shown");
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
        console.log("Roomify: hideMap called");

        var map = document.getElementById("google-map-container");
        if (map) {
            map.style.display = "none";
            map.style.visibility = "hidden";
            map.style.opacity = "0";
            map.style.zIndex = "-1";
            console.log("Roomify: Map hidden successfully");
        } else {
            console.log("Roomify: Map container not found");
        }
    };

    window.roomifyShowMapDirect = function() {
        console.log("Roomify: showMap called");

        var map = document.getElementById("google-map-container");
        if (map) {
            map.style.display = "block";
            map.style.visibility = "visible";
            map.style.opacity = "1";
            map.style.zIndex = "1";
            console.log("Roomify: Map shown successfully");
        } else {
            console.log("Roomify: Map container not found");
        }
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
                overflow: hidden;
                border: 1px solid #BDBDBD;
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
                font-size: 14px;
                color: #1A237E;
                background: transparent;
                font-weight: 500;
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


            /*
             * ====================================================
             * SIDEBAR
             * ====================================================
             */

            .roomify-sidebar {
                position: fixed;
                top: 0;
                left: 0;
                width: 310px;
                max-width: 85vw;
                height: 100vh;
                background: linear-gradient(135deg, #1A237E, #3949AB);
                z-index: 10000;
                box-shadow: 4px 0 20px rgba(0,0,0,0.20);
                transform: translateX(-105%);
                transition: transform 0.25s ease;
                display: flex;
                flex-direction: column;
                font-family: Arial, sans-serif;
                color: #ffffff;
            }

            .roomify-sidebar.open {
                transform: translateX(0);
            }

            .roomify-sidebar-header {
                height: 78px;
                padding: 0 20px;
                display: flex;
                align-items: center;
                justify-content: space-between;
                border-bottom: 1px solid rgba(255, 255, 255, 0.15);
            }

            .roomify-sidebar-brand {
                color: #ffffff;
                font-size: 21px;
                font-weight: 800;
                letter-spacing: 1px;
            }

            .roomify-sidebar-close {
                width: 38px;
                height: 38px;
                border: 0;
                border-radius: 50%;
                background: rgba(255, 255, 255, 0.12);
                cursor: pointer;
                font-size: 20px;
                color: #ffffff;
                display: flex;
                align-items: center;
                justify-content: center;
            }

            .roomify-sidebar-close:hover {
                background: rgba(255, 255, 255, 0.20);
            }

            .roomify-sidebar-body {
                flex: 1;
                padding: 18px 12px;
                overflow-y: auto;
            }

            .roomify-sidebar-section {
                margin-bottom: 22px;
            }

            .roomify-sidebar-section-title {
                padding: 0 12px 8px;
                font-size: 10px;
                font-weight: 700;
                color: rgba(255, 255, 255, 0.55);
                text-transform: uppercase;
                letter-spacing: 1.3px;
            }

            .roomify-sidebar-item {
                width: 100%;
                min-height: 48px;
                border: 0;
                background: transparent;
                border-radius: 16px;
                display: flex;
                align-items: center;
                padding: 0 12px;
                margin-bottom: 3px;
                cursor: pointer;
                text-align: left;
                color: rgba(255, 255, 255, 0.80);
                font-size: 14px;
                font-weight: 500;
                transition: background 0.15s ease;
            }

            .roomify-sidebar-item:hover {
                background: rgba(255, 255, 255, 0.10);
            }

            .roomify-sidebar-item.active {
                background: rgba(255, 255, 255, 0.14);
                color: #ffffff;
                font-weight: 700;
            }

            .roomify-sidebar-item-icon {
                width: 30px;
                font-size: 18px;
                margin-right: 9px;
                opacity: 0.85;
            }

            .roomify-sidebar-item-arrow {
                margin-left: auto;
                font-size: 14px;
                opacity: 0.30;
                color: rgba(255, 255, 255, 0.50);
            }

            .roomify-sidebar-account {
                margin: 0 12px 14px;
                padding: 14px;
                border-radius: 17px;
                background: rgba(255, 255, 255, 0.09);
                border: 1px solid rgba(255, 255, 255, 0.16);
                color: white;
            }

            .roomify-sidebar-account-title {
                font-size: 13px;
                font-weight: 700;
            }

            .roomify-sidebar-account-subtitle {
                margin-top: 4px;
                font-size: 11px;
                opacity: 0.70;
            }


            /*
             * ====================================================
             * SIDEBAR BACKDROP
             * ====================================================
             */

            .roomify-sidebar-backdrop {
                position: fixed;
                inset: 0;
                background: rgba(0,0,0,0.28);
                z-index: 9999;
                display: none;
            }

            .roomify-sidebar-backdrop.open {
                display: block;
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
                font-size: 18px;
                font-weight: 700;
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
                font-size: 12px;
                color: rgba(255,255,255,0.90);
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
                font-size: 19px;
                font-weight: 800;
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
        `;

        var input =
            wrapper.querySelector(
                "#roomify-search-input"
            );

        var clear =
            wrapper.querySelector(
                "#roomify-search-clear"
            );

        /*
         * Prevent map gestures when interacting with search.
         */

        wrapper.addEventListener(
            "click",
            function (event) {

                event.stopPropagation();
            }
        );

        wrapper.addEventListener(
            "mousedown",
            function (event) {

                event.stopPropagation();
            }
        );

        wrapper.addEventListener(
            "touchstart",
            function (event) {

                event.stopPropagation();
            },
            {
                passive: true
            }
        );


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

                filterRooms(
                    query
                );
            }
        );


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

        if (
            document.getElementById(
                "roomify-menu-button"
            )
        ) {
            return;
        }

        var button =
            document.createElement("button");

        button.id =
            "roomify-menu-button";

        button.className =
            "roomify-menu-button";

        button.type =
            "button";

        button.innerHTML =
            "☰";

        button.setAttribute(
            "aria-label",
            "Open Roomify menu"
        );

        button.addEventListener(
            "click",
            function (event) {

                event.preventDefault();
                event.stopPropagation();

                toggleSidebar();
            }
        );

        return button;
    }


    /*
     * ============================================================
     * LOCALIZATION
     * ============================================================
     */

    window.roomifyLocalization = {
        explore: "Explore",
        dashboard: "Dashboard",
        savedRooms: "Saved Rooms",
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
                    window.roomifyCurrentUser.initials
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

        if (
            document.getElementById(
                "roomify-sidebar"
            )
        ) {
            return;
        }

        var backdrop =
            document.createElement("div");

        backdrop.id =
            "roomify-sidebar-backdrop";

        backdrop.className =
            "roomify-sidebar-backdrop";


        var sidebar =
            document.createElement("aside");

        sidebar.id =
            "roomify-sidebar";

        sidebar.className =
            "roomify-sidebar";


        sidebar.innerHTML = `

            <div class="roomify-sidebar-header">

                <div class="roomify-sidebar-brand">
                    ROOMIFY
                </div>

                <button
                    id="roomify-sidebar-close"
                    class="roomify-sidebar-close"
                    type="button"
                >
                    ×
                </button>

            </div>


            <div class="roomify-sidebar-body">

                <div class="roomify-sidebar-section">

                    <div
                        class="roomify-sidebar-section-title"
                    >
                        DISCOVER
                    </div>

                    <button
                        class="roomify-sidebar-item active"
                        data-roomify-menu="explore"
                    >
                        <span
                            class="roomify-sidebar-item-icon"
                        >
                            🏠
                        </span>

                        <span>
                            Explore
                        </span>

                        <span class="roomify-sidebar-item-arrow">
                            →
                        </span>
                    </button>


                    <button
                        class="roomify-sidebar-item"
                        data-roomify-menu="saved"
                    >
                        <span
                            class="roomify-sidebar-item-icon"
                        >
                            ♡
                        </span>

                        <span>
                            Saved Properties
                        </span>

                        <span class="roomify-sidebar-item-arrow">
                            →
                        </span>
                    </button>


                    <button
                        class="roomify-sidebar-item"
                        data-roomify-menu="searches"
                    >
                        <span
                            class="roomify-sidebar-item-icon"
                        >
                            ◷
                        </span>

                        <span>
                            My Searches
                        </span>

                        <span class="roomify-sidebar-item-arrow">
                            →
                        </span>
                    </button>

                </div>


                <div class="roomify-sidebar-section">

                    <div
                        class="roomify-sidebar-section-title"
                    >
                        SEARCH
                    </div>

                    <button
                        class="roomify-sidebar-item"
                        data-roomify-menu="filters"
                    >
                        <span
                            class="roomify-sidebar-item-icon"
                        >
                            ⚙
                        </span>

                        <span>
                            Filters
                        </span>

                        <span class="roomify-sidebar-item-arrow">
                            →
                        </span>
                    </button>

                </div>


                <div class="roomify-sidebar-section">

                    <div
                        class="roomify-sidebar-section-title"
                    >
                        ACCOUNT
                    </div>

                    <button
                        class="roomify-sidebar-item"
                        data-roomify-menu="login"
                    >
                        <span
                            class="roomify-sidebar-item-icon"
                        >
                            👤
                        </span>

                        <span>
                            Login / Register
                        </span>

                        <span class="roomify-sidebar-item-arrow">
                            →
                        </span>
                    </button>

                </div>

            </div>


            <div class="roomify-sidebar-account">

                <div
                    class="roomify-sidebar-account-title"
                >
                    Find your next room
                </div>

                <div
                    class="roomify-sidebar-account-subtitle"
                >
                    Search properties around you
                </div>

            </div>
        `;


        document.body.appendChild(
            backdrop
        );

        document.body.appendChild(
            sidebar
        );

        // Core Sidebar Listeners
        document.getElementById("roomify-sidebar-close").addEventListener("click", function() {
            closeSidebar();
        });

        backdrop.addEventListener("click", function() {
            closeSidebar();
        });

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

        var sidebarBody = sidebar.querySelector(".roomify-sidebar-body");
        var accountSection = sidebar.querySelector(".roomify-sidebar-account");

        if (!sidebarBody || !accountSection) return;

        var loc = window.roomifyLocalization;
        var bodyHtml = "";

        // --- DISCOVER / MENU SECTION ---
        bodyHtml += `
            <div class="roomify-sidebar-section">
                <div class="roomify-sidebar-section-title">${role ? loc.account : "DISCOVER"}</div>
                <button class="roomify-sidebar-item active" data-roomify-menu="explore">
                    <span>${loc.explore}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
        `;

        if (role === "TENANT") {
            bodyHtml += `
                <button class="roomify-sidebar-item" data-roomify-menu="tenant">
                    <span>${loc.dashboard}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
                <button class="roomify-sidebar-item" data-roomify-menu="saved">
                    <span>${loc.savedRooms}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
                <button class="roomify-sidebar-item" data-roomify-menu="bookings">
                    <span>${loc.myBookings}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
                <button class="roomify-sidebar-item" data-roomify-menu="messages">
                    <span>${loc.messages}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
            `;
        } else if (role === "OWNER") {
            bodyHtml += `
                <button class="roomify-sidebar-item" data-roomify-menu="ownerdashboard">
                    <span>${loc.ownerDashboard}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
                <button class="roomify-sidebar-item" data-roomify-menu="postroom">
                    <span>${loc.postARoom}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
            `;
        } else if (role === "DALALI") {
            bodyHtml += `
                <button class="roomify-sidebar-item" data-roomify-menu="dalalidashboard">
                    <span>Dalali Dashboard</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
            `;
        } else if (role === "ADMIN") {
             bodyHtml += `
                <button class="roomify-sidebar-item" data-roomify-menu="admindashboard">
                    <span>${loc.adminPanel}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
            `;
        } else {
            bodyHtml += `
                <button class="roomify-sidebar-item" data-roomify-menu="saved">
                    <span>${loc.savedRooms}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
                <button class="roomify-sidebar-item" data-roomify-menu="searches">
                    <span>${loc.mySearches}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
            `;
        }

        bodyHtml += `</div>`;

        // --- ACCOUNT SECTION ---
        bodyHtml += `
            <div class="roomify-sidebar-section">
                <div class="roomify-sidebar-section-title">${role ? loc.account : loc.search}</div>
        `;

        if (!role) {
            bodyHtml += `
                <button class="roomify-sidebar-item" data-roomify-menu="filters">
                    <span>${loc.filters}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
                <button class="roomify-sidebar-item" data-roomify-menu="login">
                    <span>${loc.loginRegister}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
            `;
        } else {
            bodyHtml += `
                <button class="roomify-sidebar-item" data-roomify-menu="profile">
                    <span>${loc.profile}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
                <button class="roomify-sidebar-item" data-roomify-menu="logout">
                    <span>${loc.logout}</span>
                    <span class="roomify-sidebar-item-arrow">→</span>
                </button>
            `;
        }

        bodyHtml += `</div>`;

        // --- LANGUAGE SECTION ---
        bodyHtml += `
            <div class="roomify-sidebar-section">
                <div class="roomify-sidebar-section-title">${loc.language}</div>
                <div style="display:flex; gap:8px; padding:0 12px;">
                    <button class="roomify-sidebar-item ${loc.languageCode === 'en' ? 'active' : ''}" style="flex:1; justify-content:center; padding:8px 0;" onclick="handleLanguageChange('en')">EN</button>
                    <button class="roomify-sidebar-item ${loc.languageCode === 'sw' ? 'active' : ''}" style="flex:1; justify-content:center; padding:8px 0;" onclick="handleLanguageChange('sw')">SW</button>
                </div>
            </div>
        `;

        sidebarBody.innerHTML = bodyHtml;

        // Update Account Box at bottom
        if (role) {
            var avatarContent = initials || "U";
            if (profileImage) {
                avatarContent = `<img src="${profileImage}" style="width:100%; height:100%; border-radius:12px; object-fit:cover;" onerror="this.parentElement.innerHTML='${initials || 'U'}'" />`;
            }

            accountSection.innerHTML = `
                <div style="display:flex; align-items:center;">
                    <div style="width:36px; height:36px; border-radius:12px; background:rgba(255,255,255,0.18); display:flex; align-items:center; justify-content:center; font-weight:bold; margin-right:12px; font-size:14px; border:1px solid rgba(255,255,255,0.1); overflow:hidden;">
                        ${avatarContent}
                    </div>
                    <div style="flex:1; min-width:0;">
                        <div class="roomify-sidebar-account-title" style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${name || "User"}</div>
                        <div class="roomify-sidebar-account-subtitle" style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${email || ""}</div>
                    </div>
                </div>
            `;
        } else {
            accountSection.innerHTML = `
                <div class="roomify-sidebar-account-title">Find your next room</div>
                <div class="roomify-sidebar-account-subtitle">Search properties around you</div>
            `;
        }

        // Re-attach listeners for new items
        var items = sidebarBody.querySelectorAll("[data-roomify-menu]");
        items.forEach(function (item) {
            item.addEventListener("click", function () {
                var destination = item.getAttribute("data-roomify-menu");
                handleSidebarNavigation(destination);
            });
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
    }


    /*
     * ============================================================
     * FILTER ROOMS
     * ============================================================
     */

    function filterRooms(query) {

        var normalized =
            String(query || "")
                .trim()
                .toLowerCase();


        /*
         * No search:
         *
         * display everything.
         */

        if (!normalized) {

            updateVisibleMarkers(
                window.roomifyAllRooms
            );

            hideSearchCount();

            return;
        }


        var words =
            normalized
                .split(/\s+/)
                .filter(
                    function (word) {
                        return word.length > 0;
                    }
                );


        var filtered =
            window.roomifyAllRooms.filter(
                function (room) {

                    var searchable =
                        [
                            room.title,
                            room.address,
                            room.propertyType,
                            room.status,
                            room.city,
                            room.areaName,
                            room.location
                        ]
                            .filter(
                                function (value) {
                                    return (
                                        value !==
                                        undefined &&
                                        value !==
                                        null
                                    );
                                }
                            )
                            .join(" ")
                            .toLowerCase();


                    /*
                     * Every typed word must exist somewhere
                     * in the room information.
                     *
                     * Example:
                     *
                     * "Mikocheni apartment"
                     *
                     * both Mikocheni and apartment must match.
                     */

                    return words.every(
                        function (word) {

                            return searchable
                                .includes(word);
                        }
                    );
                }
            );


        updateVisibleMarkers(
            filtered
        );

        showSearchCount(
            filtered.length,
            window.roomifyAllRooms.length
        );


        /*
         * If one result exists, move the map to it.
         */

        if (
            filtered.length === 1
        ) {

            var room =
                filtered[0];

            var lat =
                Number(
                    room.latitude !== undefined
                        ? room.latitude
                        : room.lat
                );

            var lng =
                Number(
                    room.longitude !== undefined
                        ? room.longitude
                        : room.lng
                );

            if (
                isFinite(lat) &&
                isFinite(lng)
            ) {

                window.roomifyMap.panTo({
                    lat: lat,
                    lng: lng
                });

                window.roomifyMap.setZoom(
                    15
                );
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
                "1";


            window.roomifyMap =
                new google.maps.Map(
                    container,
                    {

                        center: {
                            lat: -6.7924,
                            lng: 39.2083
                        },

                        zoom: 12,

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
                            0
                    }
                );

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
        selected
    ) {

        var text =
            formatCompactPrice(
                price
            );

        var color =
            selected
                ? ROOMIFY_SELECTED_ORANGE
                : statusColor(status);

        var alpha =
            selected
                ? 1.0
                : statusAlpha(status);


        var svg =
            '<svg xmlns="http://www.w3.org/2000/svg" ' +
            'width="100" height="50" ' +
            'viewBox="0 0 100 50">' +

            '<defs>' +

            '<filter id="shadow" ' +
            'x="-50%" y="-50%" ' +
            'width="200%" height="200%">' +

            '<feDropShadow ' +
            'dx="0" dy="1.5" ' +
            'stdDeviation="1.8" ' +
            'flood-opacity="0.28"/>' +

            '</filter>' +

            '</defs>' +

            '<g ' +
            'filter="url(#shadow)" ' +
            'opacity="' +
            alpha +
            '">' +

            '<rect ' +
            'x="4" y="4" ' +
            'rx="14" ry="14" ' +
            'width="92" height="31" ' +
            'fill="' +
            color +
            '"/>' +

            '<path ' +
            'd="M42 35 L50 47 L58 35" ' +
            'fill="' +
            color +
            '"/>' +

            '<text ' +
            'x="50" y="24" ' +
            'text-anchor="middle" ' +
            'font-family="Arial,sans-serif" ' +
            'font-size="11" ' +
            'font-weight="700" ' +
            'fill="#ffffff">' +

            escapeHtml(text) +

            '</text>' +

            '</g>' +

            '</svg>';


        return {

            url:
                "data:image/svg+xml;charset=UTF-8," +
                encodeURIComponent(svg),

            scaledSize:
                new google.maps.Size(
                    100,
                    50
                ),

            anchor:
                new google.maps.Point(
                    50,
                    47
                )
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

        // FIX: Wrap everything in a container that stops propagation
        return (
            '<div class="roomify-popup-container-wrapper" ' +
            'onclick="event.stopPropagation();" ' +
            'onmousedown="event.stopPropagation();" ' +
            'onmouseup="event.stopPropagation();">' +

            '<div class="roomify-popup">' +

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

            '<button class="roomify-popup-button" data-roomify-view-details="' +
            escapeHtml(room.id) +
            '">' +
            'View Property Details' +
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

        if (
            !Array.isArray(
                window.roomifyMarkers
            )
        ) {
            return;
        }


        window.roomifyMarkers.forEach(
            function (entry) {

                if (!entry) {
                    return;
                }


                var selected =
                    String(
                        entry.room.id
                    ) ===
                    String(
                        window.roomifySelectedRoomId
                    );


                entry.marker.setIcon(
                    createPriceIcon(
                        entry.room.price,
                        entry.room.status,
                        selected
                    )
                );
            }
        );
    }


    window.roomifySelectRoom =
        function (roomId) {

            window.roomifySelectedRoomId =
                String(roomId);

            refreshMarkerIcons();
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
                            selected
                        ),

                    optimized:
                        true
                }
            );


        /*
         * Desktop hover.
         */

        marker.addListener(
            "mouseover",
            function () {

                showRoomInfo(
                    marker,
                    room
                );
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


            console.log(
                "Roomify: created " +
                window.roomifyMarkers.length +
                " markers"
            );
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
        "Roomify: roomify-map.js loaded"
    );

})();
