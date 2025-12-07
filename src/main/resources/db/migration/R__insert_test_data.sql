-- Repeatable migration for test/mock data
-- Only inserts data if tables are empty (won't delete existing data)

-- Insert Coaches only if table is empty
IF NOT EXISTS (SELECT 1 FROM coach)
BEGIN
    SET IDENTITY_INSERT coach ON;
    INSERT INTO coach (coach_id, first_name, last_name, rating, strike_count, coach_status) VALUES
    (1, 'John', 'Smith', 8.50, 0, 'ACTIVE'),
    (2, 'Sarah', 'Johnson', 9.20, 0, 'ACTIVE'),
    (3, 'Michael', 'Williams', 7.80, 1, 'ACTIVE'),
    (4, 'Emily', 'Brown', 9.50, 0, 'ACTIVE'),
    (5, 'David', 'Davis', 6.00, 4, 'ACTIVE');
    SET IDENTITY_INSERT coach OFF;
END

-- Insert Users only if table is empty
IF NOT EXISTS (SELECT 1 FROM app_user)
BEGIN
    SET IDENTITY_INSERT app_user ON;
    INSERT INTO app_user (user_id, first_name, last_name, sessions_taken) VALUES
    (1, 'Alice', 'Anderson', 5),
    (2, 'Bob', 'Baker', 3),
    (3, 'Charlie', 'Clark', 8),
    (4, 'Diana', 'Douglas', 2),
    (5, 'Edward', 'Evans', 10);
    SET IDENTITY_INSERT app_user OFF;
END

-- Insert Sessions only if table is empty
IF NOT EXISTS (SELECT 1 FROM session)
BEGIN
    SET IDENTITY_INSERT session ON;

    -- Completed sessions with ratings
    INSERT INTO session (session_id, session_date_time, session_status, coach_id, user_id, rating) VALUES
    (1, '2024-12-01 10:00:00', 'COMPLETED', 1, 1, 8.50),
    (2, '2024-12-02 14:00:00', 'COMPLETED', 2, 2, 9.00),
    (3, '2024-12-03 09:00:00', 'COMPLETED', 3, 3, 7.50),
    (4, '2024-12-04 11:00:00', 'COMPLETED', 4, 4, 9.50),
    (5, '2024-12-05 16:00:00', 'COMPLETED', 5, 5, 6.00),
    (6, '2024-12-06 10:00:00', 'COMPLETED', 1, 3, 8.00),
    (7, '2024-12-07 13:00:00', 'COMPLETED', 2, 1, 9.20);

    -- Scheduled sessions (future, no rating yet)
    INSERT INTO session (session_id, session_date_time, session_status, coach_id, user_id, rating) VALUES
    (8, '2024-12-15 10:00:00', 'SCHEDULED', 1, 2, NULL),
    (9, '2024-12-16 14:00:00', 'SCHEDULED', 3, 4, NULL),
    (10, '2024-12-17 09:00:00', 'SCHEDULED', 4, 5, NULL),
    (11, '2024-12-18 11:00:00', 'SCHEDULED', 2, 3, NULL),
    (12, '2024-12-20 15:00:00', 'SCHEDULED', 5, 1, NULL);

    -- Cancelled sessions
    INSERT INTO session (session_id, session_date_time, session_status, coach_id, user_id, rating) VALUES
    (13, '2024-12-08 10:00:00', 'CANCELLED', 3, 2, NULL),
    (14, '2024-12-09 14:00:00', 'CANCELLED', 5, 4, NULL);

    SET IDENTITY_INSERT session OFF;
END
