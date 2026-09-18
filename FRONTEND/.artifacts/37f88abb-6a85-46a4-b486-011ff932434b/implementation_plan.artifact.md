# Implementation Plan - Consolidate Discovery Features and Navigation into Sidebar

This plan aims to centralize the navigation and discovery features of Roomify into the sidebar (menu icon). Currently, `DiscoveryDashboard` acts as a separate landing page, and the sidebar has limited navigation options for guest users. We will move the key discovery filters and add navigation to the Furniture Hub directly into the sidebar for both Android and Web.

## User Review Required

> [!IMPORTANT]
> The Sidebar will now become the primary hub for both Room and Furniture discovery. The current `DiscoveryDashboard` (landing page) will remain as the initial entry point, but all its functionality (filtering) will be duplicated or accessible via the Sidebar for easier access while on the map.

## Proposed Changes

### [Component] Navigation & Sidebar (Common/Android)

#### [MODIFY] [MapContent.kt](file:///C:/Users/user/StudioProjects/ROOMIFY/FRONTEND/shared/src/androidMain/kotlin/org/com/ui/MapContent.kt)
- Update `RoomifySideBar` to include:
    - A new "FURNITURE" section.
    - Navigation items for "Furniture Hub" and "Post Furniture".
    - A "SEARCH & FILTERS" section that mirrors the features of `DiscoveryDashboard` (Area, Price, Type filters) or provides a quick way to trigger them.
    - Ensure the guest mode sidebar includes these items.

#### [MODIFY] [App.kt](file:///C:/Users/user/StudioProjects/ROOMIFY/FRONTEND/shared/src/commonMain/kotlin/org/com/App.kt)
- Update `navigateTo` to ensure `furniture_dashboard` and other new routes are correctly handled.
- Ensure the `onNavigate` callback passed to `MapContent` is fully utilized.

### [Component] Web/Wasm Map Implementation

#### [MODIFY] [roomify-map.js](file:///C:/Users/user/StudioProjects/ROOMIFY/FRONTEND/webApp/src/wasmJsMain/resources/roomify-map.js)
- Update `roomifyUpdateUser` JS function to add the new "FURNITURE" section to the DOM sidebar.
- Add "Furniture Hub" and "Post Furniture" items to the sidebar list.
- Update the sidebar CSS if necessary to accommodate more items.

#### [MODIFY] [MapContent.kt](file:///C:/Users/user/StudioProjects/ROOMIFY/FRONTEND/shared/src/wasmJsMain/kotlin/org/com/ui/MapContent.kt)
- Ensure the `registerGlobalNavigationListener` handles the new furniture-related navigation events dispatched from the JS sidebar.

## Verification Plan

### Automated Tests
- N/A (UI-centric refactoring).

### Manual Verification
1. **Android**:
    - Open the App, enter Guest Mode (or log in).
    - Open the Map and tap the Menu Icon (Top-Left).
    - Verify "Furniture Hub" is visible in the sidebar.
    - Tap "Furniture Hub" and verify it navigates to `FurnitureDashboard.kt`.
    - Verify that Discovery-like filters are accessible from the sidebar.
2. **Web/Wasm**:
    - Open Roomify in a browser.
    - Open the Map and click the Menu Icon (Top-Left).
    - Verify the sidebar contains the new "Furniture" section.
    - Click "Furniture Hub" and verify the Compose layer updates to show the furniture dashboard.
