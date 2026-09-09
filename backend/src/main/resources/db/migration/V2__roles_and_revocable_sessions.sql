CREATE TABLE user_platform_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    platform_role_id BIGINT NOT NULL REFERENCES platform_roles(id),
    PRIMARY KEY(user_id, platform_role_id)
);

ALTER TABLE user_sessions ADD COLUMN access_key VARCHAR(36);
UPDATE user_sessions SET access_key = gen_random_uuid()::text, active = false;
ALTER TABLE user_sessions ALTER COLUMN access_key SET NOT NULL;
CREATE UNIQUE INDEX uk_session_access_key ON user_sessions(access_key);

INSERT INTO roles(name, description, active, created_at, updated_at) VALUES
    ('OWNER', 'Company owner', true, now(), now()),
    ('ADMIN', 'Company administrator', true, now(), now()),
    ('MANAGER', 'Company manager', true, now(), now()),
    ('MEMBER', 'Regular company user', true, now(), now())
ON CONFLICT (name) DO NOTHING;
INSERT INTO platform_roles(name, description, active, created_at, updated_at) VALUES
    ('SUPERADMIN', 'Platform super administrator', true, now(), now()),
    ('ADMIN', 'Platform administrator', true, now(), now())
ON CONFLICT (name) DO NOTHING;

UPDATE users SET status = 'ACTIVE' WHERE status = 'PENDING_VERIFICATION' AND email_verified = true;
