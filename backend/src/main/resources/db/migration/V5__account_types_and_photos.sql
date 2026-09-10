ALTER TABLE users ADD COLUMN IF NOT EXISTS account_type VARCHAR(30) NOT NULL DEFAULT 'INDIVIDUAL';
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_photo_url VARCHAR(1000);

ALTER TABLE companies ADD COLUMN IF NOT EXISTS logo_url VARCHAR(1000);

ALTER TABLE company_profiles ALTER COLUMN legal_name DROP NOT NULL;
ALTER TABLE company_profiles DROP COLUMN IF EXISTS registration_number;
ALTER TABLE company_profiles DROP COLUMN IF EXISTS tax_id;

ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS account_type VARCHAR(30) NOT NULL DEFAULT 'ORGANIZATION';
UPDATE subscription_plans SET code = 'FREE_ORGANIZATION', name = 'Free Organization', account_type = 'ORGANIZATION'
WHERE code = 'FREE';

INSERT INTO subscription_plans(code, account_type, name, monthly_price, currency, active, created_at, updated_at)
VALUES ('FREE_INDIVIDUAL', 'INDIVIDUAL', 'Free Individual', 0.00, 'INR', true, now(), now())
ON CONFLICT (code) DO NOTHING;

INSERT INTO subscription_plan_limits(plan_id, feature_key, limit_value, unit, created_at, updated_at)
SELECT p.id, limits.feature_key, limits.limit_value, limits.unit, now(), now()
FROM subscription_plans p
CROSS JOIN (
    VALUES
        ('broadcast.max_recipients', 500, 'recipients'),
        ('whatsapp.max_accounts', 1, 'accounts'),
        ('sms.max_accounts', 1, 'accounts'),
        ('email.max_accounts', 1, 'accounts'),
        ('ads.max_providers', 1, 'providers'),
        ('whatsapp.templates.max_count', 1, 'templates'),
        ('email.templates.max_count', 1, 'templates'),
        ('email.daily_free_limit', 500, 'segments')
) AS limits(feature_key, limit_value, unit)
WHERE p.code = 'FREE_INDIVIDUAL'
ON CONFLICT (plan_id, feature_key) DO NOTHING;

INSERT INTO subscription_channel_pricing(
    plan_id, channel, message_type, price_per_segment, free_daily_segments, currency, active, created_at, updated_at)
SELECT p.id, pricing.channel, pricing.message_type, pricing.price_per_segment, pricing.free_daily_segments, 'INR', true, now(), now()
FROM subscription_plans p
CROSS JOIN (
    VALUES
        ('EMAIL', 'PROMOTIONAL', 0.2000, 500),
        ('EMAIL', 'CONVERSATIONAL', 0.2000, 500),
        ('WHATSAPP', 'PROMOTIONAL', 0.9000, 0),
        ('WHATSAPP', 'CONVERSATIONAL', 0.9000, 0),
        ('SMS', 'PROMOTIONAL', 0.0000, 0),
        ('SMS', 'CONVERSATIONAL', 0.0000, 0)
) AS pricing(channel, message_type, price_per_segment, free_daily_segments)
WHERE p.code = 'FREE_INDIVIDUAL'
ON CONFLICT (plan_id, channel, message_type) DO NOTHING;
