-- This script is now 100% in sync with the User.java entity.

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    -- Matches @Id @Column(name = "user_id")
                                     user_id BIGSERIAL PRIMARY KEY,

    -- Matches @Column(unique = true, nullable = false)
                                     email VARCHAR(255) UNIQUE NOT NULL,

    -- Matches @Column(length = 255)
    password VARCHAR(255) NOT NULL,

    -- Matches @Column(name = "first_name")
    first_name VARCHAR(50) NOT NULL,

    -- Matches @Column(name = "last_name")
    last_name VARCHAR(50) NOT NULL,

    -- Matches the optional phoneNumber field in the entity
    phone_number VARCHAR(20),

    -- Matches @Enumerated(EnumType.STRING)
    role VARCHAR(20) NOT NULL DEFAULT 'USER',

    -- Matches @Column(name = "is_active")
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- NEW: Matches @Column(name = "is_email_verified")
    is_email_verified BOOLEAN NOT NULL DEFAULT false,

    -- We let the application handle the timestamps via @PrePersist for better control.
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    last_login_at TIMESTAMP
    );

-- Create indexes with names matching the entity's @Index annotations
CREATE INDEX IF NOT EXISTS idx_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_role ON users(role);

-- Insert default admin user.
-- The password hash is for 'admin123'. The admin user is created as active and email-verified.
INSERT INTO users (email, password, first_name, last_name, phone_number, role, is_active, is_email_verified, created_at, updated_at)
VALUES (
           'mdbakibillahrahat@gmail.com',
           '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P2NRA9hJmX6dB2',
           'MD. Bakibillah',
           'Rahat',
           '+8801909562232',
           'ADMIN',
           true, -- is_active
           true, -- is_email_verified (so the admin can log in immediately)
           NOW(), -- created_at
           NOW()  -- updated_at
       ) ON CONFLICT (email) DO NOTHING;
