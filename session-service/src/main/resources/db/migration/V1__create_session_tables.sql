-- Create User table
CREATE TABLE app_users (
    user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100) NOT NULL,
    sessions_taken INT DEFAULT 0
);

-- Create Session table (coach_id is a reference to CoachService, no FK constraint)
CREATE TABLE sessions (
    session_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    session_date_time DATETIME2 NOT NULL,
    session_status NVARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    coach_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating DECIMAL(3,2) NULL,
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES app_users(user_id),
    CONSTRAINT chk_session_status CHECK (session_status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_session_rating CHECK (rating IS NULL OR (rating >= 0 AND rating <= 10))
);

-- Create indexes
CREATE INDEX idx_session_coach_id ON sessions(coach_id);
CREATE INDEX idx_session_user_id ON sessions(user_id);
CREATE INDEX idx_session_status ON sessions(session_status);
