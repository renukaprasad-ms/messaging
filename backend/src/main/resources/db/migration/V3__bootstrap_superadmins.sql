-- Existing accounts keep their password and status. New accounts must change their temporary password.
ALTER TABLE users ADD COLUMN password_change_required BOOLEAN NOT NULL DEFAULT false;

INSERT INTO users(name, email, password, status, email_verified, phone_verified, password_change_required, created_at, updated_at)
VALUES ('Renuka Prasad', 'renukaprasadms00@gmail.com', '$2b$10$wpv36oxzqqdi8KMopqyoZ.PPiJ3WGbWMD93OEfHNCLReMMTwSC42e', 'ACTIVE', true, false, true, now(), now())
ON CONFLICT (email) DO NOTHING;
INSERT INTO user_platform_roles(user_id, platform_role_id)
SELECT u.id, r.id FROM users u CROSS JOIN platform_roles r
WHERE u.email = 'renukaprasadms00@gmail.com' AND r.name = 'SUPERADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO users(name, email, password, status, email_verified, phone_verified, password_change_required, created_at, updated_at)
VALUES ('Renuka Prasad', 'renukaprasad.dev@gmail.com', '$2b$10$L1IcIjMnC5FtXN1.xzib1ePGtJEry2es6V8FcHdvYVb7MRS6uu9ra', 'ACTIVE', true, false, true, now(), now())
ON CONFLICT (email) DO NOTHING;
INSERT INTO user_platform_roles(user_id, platform_role_id)
SELECT u.id, r.id FROM users u CROSS JOIN platform_roles r
WHERE u.email = 'renukaprasad.dev@gmail.com' AND r.name = 'SUPERADMIN'
ON CONFLICT DO NOTHING;

