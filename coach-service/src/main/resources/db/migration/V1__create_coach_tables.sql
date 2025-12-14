-- Create Coach table
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

CREATE INDEX idx_coach_status ON coaches(coach_status);
