-- ============================================
-- ARCHIX - Database Initialization Script
-- ============================================
-- This script runs on first PostgreSQL container start
-- Creates the database and necessary extensions

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create schema (optional, using public by default)
-- CREATE SCHEMA IF NOT EXISTS archix;

-- Grant privileges (if needed for specific users)
-- GRANT ALL PRIVILEGES ON DATABASE archix_db TO archix;

-- Log completion
SELECT 'Database initialization completed at ' || NOW();
