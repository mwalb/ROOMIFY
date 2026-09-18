# Enhanced Marker and Clustering System Implementation Plan

This plan outlines the enhancements to the Roomify map marker system, including clustering, zoom-dependent marker types, and default location settings for both Web/Wasm and Android platforms.

## User Review Required

> [!IMPORTANT]
> The implementation will introduce a clustering library for the Web platform (`MarkerClusterer`) and manual clustering logic for Android to meet the visual requirements.

> [!WARNING]
> Default map coordinates for Dar es Salaam will be applied only when no other location context (search, selection, etc.) is present.

## Proposed Changes

### Web/Wasm Implementation

#### [MODIFY] [index.html](file:///C:/Users/user/StudioProjects/ROOMIFY/FRONTEND/webApp/src/wasmJsMain/resources/index.html)
- Add the Google Maps MarkerClusterer library script tag.

#### [MODIFY] [roomify-map.js](file:///C:/Users/user/StudioProjects/ROOMIFY/FRONTEND/webApp/src/wasmJsMain/resources/roomify-map.js)
- Update `roomifyCreateMap` to set default zoom to `11`.
- Implement `createDotIcon(room, selected, hovered, viewed, saved)` for zoom 10-13.
- Implement `createClusterIcon(count)` for zoom < 10.
- Update `refreshMarkerIcons` to handle zoom-level icons.
- Add `zoom_changed` listener to trigger marker refreshes.
- Integrate `MarkerClusterer` for zoom < 10.
- Add local tracking for `viewedRooms` and `savedRooms` to support visual states.

---

### Android Implementation

#### [MODIFY] [MapContent.kt](file:///C:/Users/user/StudioProjects/ROOMIFY/FRONTEND/shared/src/androidMain/kotlin/org/com/ui/MapContent.kt)
- Update `cameraPositionState` initialization to zoom `11f`.
- Implement zoom-dependent marker rendering logic within the `GoogleMap` composable.
- Add `createDotMarker` helper to generate dot icons for zoom 10-13.
- Implement a simple clustering renderer or grouping logic for zoom < 10.
- Add state tracking for viewed/saved markers if not already present.

---

### Shared Components

#### [MODIFY] [MapViewModel.kt](file:///C:/Users/user/StudioProjects/ROOMIFY/FRONTEND/shared/src/commonMain/kotlin/org/com/viewmodel/MapViewModel.kt)
- Add state for `viewedRoomIds` and `savedRoomIds` (if needed) to synchronize across platforms.

## Verification Plan

### Automated Tests
- N/A (UI-centric changes)

### Manual Verification
1. Open Roomify on Web and Android; verify the map starts at Dar es Salaam (zoom 11).
2. Zoom out (< 10) and verify cluster markers appear with readable numbers and ~50px diameter.
3. Zoom in (10-13) and verify price pills disappear and dots (14-18px) appear.
4. Zoom in further (> 13) and verify price pills (36-44px height) appear.
5. Verify clicking/hovering markers updates their size/style according to requirements.
6. Verify "viewed" and "saved" states are visually reflected on markers.
7. Verify search bar and menu icon remain functional and visible at all zoom levels.
