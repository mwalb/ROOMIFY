# Implementation Plan - Fix Roomify Backend Failures

This plan addresses the HTTP 500 errors on `/api/rooms` and `/api/bookings/owner/{id}`, fixes a coordinate display bug in the frontend, and ensures the database schema matches the `Room` entity.

## User Review Required

> [!IMPORTANT]
> The database schema will be updated to include missing columns (`property_id`, `floor_number`, `unit_number`). Existing data will be preserved.
> The frontend `MapIntegration.kt` will be modified to correctly display longitude in the address string.

## Proposed Changes

### Backend - Database Schema & Entity

#### [MODIFY] [Room.java](file:///C:/Users/user/StudioProjects/ROOMIFY/BACKEND/src/main/java/com/ROOMIFY/Roomify/model/Room.java)
- Uncomment `floorNumber` and `unitNumber` fields to ensure they are recognized by Hibernate.
- Verify that `property_id` is correctly mapped.

#### [NEW] [schema_update.sql](file:///C:/Users/user/StudioProjects/ROOMIFY/database/schema_update.sql)
- Create a script with the necessary `ALTER TABLE` statements to add `property_id`, `floor_number`, and `unit_number` to the `rooms` table.
- Since `psql` is not directly available in the shell path, I will provide these commands for the user to run, OR attempt to execute them via a temporary Spring Boot `CommandLineRunner` if the user prefers an automated fix.
- **SQL to execute**:
  ```sql
  ALTER TABLE rooms ADD COLUMN IF NOT EXISTS property_id BIGINT;
  ALTER TABLE rooms ADD COLUMN IF NOT EXISTS floor_number INTEGER;
  ALTER TABLE rooms ADD COLUMN IF NOT EXISTS unit_number VARCHAR(255);
  ALTER TABLE rooms ADD CONSTRAINT fk_room_property FOREIGN KEY (property_id) REFERENCES properties(id);
  ```

### Backend - Controller & Logic

#### [MODIFY] [RoomController.java](file:///C:/Users/user/StudioProjects/ROOMIFY/BACKEND/src/main/java/com/ROOMIFY/Roomify/controller/RoomController.java)
- Ensure that the `addRoom` method handles the `postedBy` and status fields correctly according to the business logic (PENDING for new listings).

### Frontend - UI Bugs

#### [MODIFY] [MapIntegration.kt](file:///C:/Users/user/StudioProjects/ROOMIFY/FRONTEND/shared/src/wasmJsMain/kotlin/org/com/ui/MapIntegration.kt)
- Fix the `formattedAddress` generation at line 666 to use `lngVal.toFixed(6)` instead of repeating `latVal.toFixed(6)`.

## Verification Plan

### Automated Tests
- N/A (Manual verification of endpoints)

### Manual Verification
1. **Database Update**: Execute the `ALTER TABLE` commands.
2. **GET /api/rooms**: Verify it returns HTTP 200 and a list of rooms.
3. **POST /api/rooms**: Create a new room and verify it is stored in the database with correct status and coordinates.
4. **GET /api/bookings/owner/8**: Verify the query no longer fails due to missing `property_id`.
5. **Frontend Check**: Open the map, pin a location, and verify the address string shows both Latitude and Longitude.
6. **Map Data**: Verify that `MapViewModel` correctly logs the number of rooms received and that they appear on the map.
