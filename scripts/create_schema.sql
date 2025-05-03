-- Drop schema if exists
DROP SCHEMA IF EXISTS dail CASCADE;

-- Create schema
CREATE SCHEMA dail;

-- Grant all permissions on schema to user
GRANT ALL ON SCHEMA dail TO dailogi_service;

-- Output confirmation
SELECT 'Schema dail created and permissions granted successfully.' AS result; 