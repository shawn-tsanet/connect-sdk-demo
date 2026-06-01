-- Creates a demo company with contact document, active version, and API user.
-- Usage:
--   psql ... -v company_name='Demo Alpha' -v company_slug='demo-alpha' -v user_email='alpha@demo.local' -f create-company.sql

\set ON_ERROR_STOP on

BEGIN;

INSERT INTO companies (
  name, full_name, slug, status, membership_type, login_method,
  community_key_codes, use_filter, use_group_login, show_in_public_web, allow_super_admins,
  created_at, updated_at
)
SELECT
  :'company_name',
  :'company_name',
  :'company_slug',
  'active', 'standard', 'password',
  false, false, false, false, false,
  NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE slug = :'company_slug');

UPDATE companies
SET name = :'company_name', full_name = :'company_name', updated_at = NOW()
WHERE slug = :'company_slug';

INSERT INTO contact_documents (
  company_id, name, is_parent, is_default, notify_admin, notify_on_change, created_at, updated_at
)
SELECT c.id, :'company_name' || ' contact form', false, true, false, false, NOW(), NOW()
FROM companies c
WHERE c.slug = :'company_slug'
  AND NOT EXISTS (
    SELECT 1 FROM contact_documents d WHERE d.company_id = c.id AND d.is_default = true
  );

INSERT INTO contact_document_versions (
  document_id, title, status, document_type, is_twentyfour_hour, created_at, updated_at
)
SELECT d.id, 'Default form', 'approved', 'ticket', false, NOW(), NOW()
FROM companies c
JOIN contact_documents d ON d.company_id = c.id AND d.is_default = true
WHERE c.slug = :'company_slug'
  AND NOT EXISTS (SELECT 1 FROM contact_document_versions v WHERE v.document_id = d.id);

INSERT INTO users (
  company_id, email, username, first_name, last_name, status, type, role,
  registration_type, failed_login_attempts, account_verified, pass_reset_flag,
  requires_approval, force_update_profile, is_global_manager, created_at, updated_at
)
SELECT
  c.id, :'user_email', :'user_email',
  split_part(:'user_email', '@', 1), 'Demo',
  'active', 'admin', 'api', 'system',
  0, true, false, false, false, false, NOW(), NOW()
FROM companies c
WHERE c.slug = :'company_slug'
  AND NOT EXISTS (SELECT 1 FROM users u WHERE LOWER(u.email) = LOWER(:'user_email'));

UPDATE users
SET company_id = c.id, updated_at = NOW()
FROM companies c
WHERE c.slug = :'company_slug'
  AND LOWER(users.email) = LOWER(:'user_email');

COMMIT;

SELECT c.id || '|' || d.id || '|' || u.id || '|' || c.slug || '|' || u.email
FROM companies c
JOIN contact_documents d ON d.company_id = c.id AND d.is_default = true
JOIN users u ON u.company_id = c.id AND LOWER(u.email) = LOWER(:'user_email')
WHERE c.slug = :'company_slug'
LIMIT 1;
