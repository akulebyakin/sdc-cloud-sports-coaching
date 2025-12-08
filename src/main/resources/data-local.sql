-- Test data for local development (H2)

-- Test data for coaches
INSERT INTO coaches (first_name, last_name, rating, strike_count, coach_status) VALUES
('John', 'Smith', 8.50, 0, 'ACTIVE'),
('Sarah', 'Johnson', 9.20, 0, 'ACTIVE'),
('Michael', 'Williams', 7.80, 1, 'ACTIVE'),
('Emily', 'Brown', 9.50, 0, 'ACTIVE'),
('David', 'Davis', 6.00, 4, 'ACTIVE');

-- Test data for users
INSERT INTO app_users (first_name, last_name, sessions_taken) VALUES
('Alice', 'Anderson', 5),
('Bob', 'Baker', 3),
('Charlie', 'Clark', 8),
('Diana', 'Douglas', 2),
('Edward', 'Evans', 10);

-- Test data for sessions
INSERT INTO sessions (session_date_time, session_status, coach_id, user_id, rating) VALUES
('2024-12-01 10:00:00', 'COMPLETED', 1, 1, 8.50),
('2024-12-02 14:00:00', 'COMPLETED', 2, 2, 9.00),
('2024-12-03 09:00:00', 'COMPLETED', 3, 3, 7.50),
('2024-12-04 11:00:00', 'COMPLETED', 4, 4, 9.50),
('2024-12-05 16:00:00', 'COMPLETED', 5, 5, 6.00),
('2024-12-06 10:00:00', 'COMPLETED', 1, 3, 8.00),
('2024-12-07 13:00:00', 'COMPLETED', 2, 1, 9.20),
('2024-12-15 10:00:00', 'SCHEDULED', 1, 2, NULL),
('2024-12-16 14:00:00', 'SCHEDULED', 3, 4, NULL),
('2024-12-17 09:00:00', 'SCHEDULED', 4, 5, NULL),
('2024-12-18 11:00:00', 'SCHEDULED', 2, 3, NULL),
('2024-12-20 15:00:00', 'SCHEDULED', 5, 1, NULL),
('2024-12-08 10:00:00', 'CANCELLED', 3, 2, NULL),
('2024-12-09 14:00:00', 'CANCELLED', 5, 4, NULL);
