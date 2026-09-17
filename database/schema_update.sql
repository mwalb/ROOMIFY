-- Schema update for Roomify
-- Run these commands in your PostgreSQL database (e.g., via psql or a GUI tool)

-- Add missing columns to the rooms table
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS property_id BIGINT;
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS floor_number INTEGER;
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS unit_number VARCHAR(255);
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS max_guests INTEGER DEFAULT 1;

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_room_property') THEN
        ALTER TABLE rooms ADD CONSTRAINT fk_room_property FOREIGN KEY (property_id) REFERENCES properties(id);
    END IF;
END $$;
