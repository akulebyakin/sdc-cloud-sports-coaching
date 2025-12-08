-- Flyway Migration V1: Create initial database schema for Sports Coaching Platform

-- Create Coach table first (referenced by Session)
CREATE TABLE coaches (
    coach_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100) NOT NULL,
    rating DECIMAL(3,2) DEFAULT 0.00,
    strike_count INT DEFAULT 0,
    coach_status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_coach_status CHECK (coach_status IN ('ACTIVE', 'DEACTIVATED')),
    CONSTRAINT chk_rating CHECK (rating >= 0 AND rating <= 10)
);

-- Create User table (referenced by Session)
CREATE TABLE app_users (
    user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100) NOT NULL,
    sessions_taken INT DEFAULT 0
);

-- Create Session table with foreign keys
CREATE TABLE sessions (
    session_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    session_date_time DATETIME2 NOT NULL,
    session_status NVARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    coach_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating DECIMAL(3,2) NULL,
    CONSTRAINT fk_session_coach FOREIGN KEY (coach_id) REFERENCES coaches(coach_id),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES app_users(user_id),
    CONSTRAINT chk_session_status CHECK (session_status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_session_rating CHECK (rating IS NULL OR (rating >= 0 AND rating <= 10))
);

-- Create indexes for better query performance
CREATE INDEX idx_session_coach_id ON sessions(coach_id);
CREATE INDEX idx_session_user_id ON sessions(user_id);
CREATE INDEX idx_session_status ON sessions(session_status);
CREATE INDEX idx_coach_status ON coaches(coach_status);
