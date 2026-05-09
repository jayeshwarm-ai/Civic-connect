-- ════════════════════════════════════════════════════════════════════════
-- CivicConnect — service_requests table migration
-- Replaces the old `location` column with `state`, `city`, and `address`.
--
-- Run this ONCE against the service_request database (the one used by
-- service-request-service) BEFORE you start the new build of the service.
-- JPA's `ddl-auto=update` will add columns but will NOT drop the old one
-- and will not change column nullability, so this script handles both.
--
-- IMPORTANT — running order:
--   1. Stop the service-request-service.
--   2. Run this SQL.
--   3. Start the new build.
-- ════════════════════════════════════════════════════════════════════════

USE servicerequest_db;   -- ← change to your actual DB name if different

-- 1. Add the three new columns (initially nullable so the ALTER doesn't fail
--    on existing rows that have no value for them yet)
ALTER TABLE service_requests
    ADD COLUMN state   VARCHAR(60)  NULL,
    ADD COLUMN city    VARCHAR(80)  NULL,
    ADD COLUMN address VARCHAR(255) NULL;

-- 2. Backfill existing rows so the NOT NULL constraint can be applied below.
--    We dump the old free-form `location` into the new `address` column and
--    use placeholders for state and city. You can refine these by hand later.
UPDATE service_requests
   SET state   = COALESCE(state,   'UNKNOWN'),
       city    = COALESCE(city,    'UNKNOWN'),
       address = COALESCE(address, location, 'Not provided')
 WHERE state IS NULL OR city IS NULL OR address IS NULL;

-- 3. Lock down the new columns: make them NOT NULL.
ALTER TABLE service_requests
    MODIFY COLUMN state   VARCHAR(60)  NOT NULL,
    MODIFY COLUMN city    VARCHAR(80)  NOT NULL,
    MODIFY COLUMN address VARCHAR(255) NOT NULL;

-- 4. Drop the old `location` column.
ALTER TABLE service_requests
    DROP COLUMN location;
