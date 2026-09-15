# Fix Admin Auth and Discovery Dashboard UI

The user is experiencing issues logging in as Admin/Super Admin even with "correct" credentials. Additionally, the Discovery Dashboard needs UI improvements to use dropdowns for Property Status and Property Type.

## User Review Required

> [!IMPORTANT]
> The Admin login issue seems to be caused by a mismatch between the emails created in the background (`raphaelfrank02@gmail.com`) and the ones potentially expected or shown in UI examples (`admin@roomify.com`). I will ensure both sets are initialized.

## Proposed Changes

### Backend

#### [MODIFY] [RoomRepository.java](file:///C:/Users/user/StudioProjects/ROOMIFY/BACKEND/src/main/java/com/ROOMIFY/Roomify/repository/RoomRepository.java)
- Add `findDistinctPropertyTypes()` to fetch unique property types from the database.

#### [MODIFY] [RoomController.java](file:///C:/Users/user/StudioProjects/ROOMIFY/BACKEND/src/main/java/com/ROOMIFY/Roomify/controller/RoomController.java)
- Add `/api/rooms/types` endpoint to expose distinct property types.

#### [MODIFY] [DataInitializer.java](file:///C:/Users/user/StudioProjects/ROOMIFY/BACKEND/src/main/java/com/ROOMIFY/Roomify/config/DataInitializer.java) and [RoomifyApplication.java](file:///C:/Users/user/StudioProjects/ROOMIFY/BACKEND/src/main/java/com/ROOMIFY/Roomify/RoomifyApplication.java)
- Add `admin@roomify.com` and `superadmin@roomify.com` to the default users to match UI expectations.

### Frontend

#### [MODIFY] [RoomifyApi.kt](file:///C:/Users/user/StudioProjects/ROOMIFY/FRONTEND/shared/src/commonMain/kotlin/org/com/network/RoomifyApi.kt)
- Add `getPropertyTypes()` method.

#### [MODIFY] [DiscoveryDashboard.kt](file:///C:/Users/user/StudioProjects/ROOMIFY/FRONTEND/shared/src/commonMain/kotlin/org/com/ui/DiscoveryDashboard.kt)
- Replace `FilterChip` groups for **Status** and **Type** with `Dropdown` menus.
- Fetch available property types from the backend on initialization.

## Verification Plan

### Automated Tests
- N/A (UI and Data initialization focused)

### Manual Verification
- Log in using `admin@roomify.com` / `Raphael11111`.
- Verify that "Status" and "Type" are now dropdowns in the Discovery Dashboard.
- Verify that the "Type" dropdown contains values currently in the database (or defaults if empty).
