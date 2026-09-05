# Implementation Plan: Room Details Enhancements

Enhance the `PropertyDetailsScreen` to provide a comprehensive view of room details, including support for images, videos, documents, and location navigation.

## User Review Required

> [!IMPORTANT]
> The implementation of Video and Document viewing will use platform-specific intents (e.g., opening the system video player or browser) to ensure compatibility across Android, iOS, and Web/Desktop, as there is currently no cross-platform media player library integrated into the project.

## Proposed Changes

### Core Model & Logic

#### [MODIFY] [Room.kt](file:///home/kali/Downloads/ROOMIFY/FRONTEND/shared/src/commonMain/kotlin/org/com/model/Room.kt)
*   Ensure all necessary fields (videoUrl, contractUrl, images, amenities, etc.) are properly defined and documented.

---

### UI Components

#### [MODIFY] [PropertyDetailsScreen.kt](file:///home/kali/Downloads/ROOMIFY/FRONTEND/shared/src/commonMain/kotlin/org/com/ui/PropertyDetailsScreen.kt)
*   **Image Pager**: Implement a horizontal pager to browse through room images.
*   **Media Actions**: Add buttons to "Play Video" and "View Document/Contract" if URLs are available.
*   **Property Details**: Display all room attributes including price, count of rooms/bathrooms, area, and status.
*   **Location Integration**: Add a "Show on Map" section or button that navigates to the map or opens the location.
*   **Amenities & Rules**: Add sections to list amenities and house rules.
*   **Contact Info**: Display owner/dalali contact details.

#### [NEW] MediaComponents.kt (file:///home/kali/Downloads/ROOMIFY/FRONTEND/shared/src/commonMain/kotlin/org/com/ui/components/MediaComponents.kt)
*   Define `expect fun openUrl(url: String)` to handle opening videos and documents in platform-native viewers.

---

### Platform Implementations

#### [NEW] MediaComponents.android.kt (file:///home/kali/Downloads/ROOMIFY/FRONTEND/shared/src/androidMain/kotlin/org/com/ui/components/MediaComponents.kt)
*   Implement `openUrl` using Android Intents.

#### [NEW] MediaComponents.ios.kt (file:///home/kali/Downloads/ROOMIFY/FRONTEND/shared/src/iosMain/kotlin/org/com/ui/components/MediaComponents.kt)
*   Implement `openUrl` using `UIApplication.sharedApplication.openURL`.

## Verification Plan

### Manual Verification
1.  Navigate to a Room Detail page from the Home or Dashboard screen.
2.  Verify the image pager scrolls through all images.
3.  Tap "Play Video" and verify it opens the system video player or browser.
4.  Tap "View Contract" and verify it opens the document URL.
5.  Verify all room details (amenities, rules, etc.) are displayed correctly.
6.  Tap "View on Map" and verify it shows the room's location.
