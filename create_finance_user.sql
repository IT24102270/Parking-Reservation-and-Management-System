-- Create a test finance executive user
-- Run this SQL script in your database

INSERT INTO [User] (FirstName, LastName, Email, PasswordHash, Role, Status, PhoneNumber, created_at, updated_at)
VALUES (
    'Finance',
    'Executive', 
    'finance@citypark.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- password: "password"
    'ROLE_FINANCE_EXECUTIVE',
    'ACTIVE',
    '1234567890',
    GETDATE(),
    GETDATE()
);

-- Test login credentials:
-- Email: finance@citypark.com  
-- Password: password
