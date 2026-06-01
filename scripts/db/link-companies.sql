-- Links two companies through a shared community so collaboration requests can be created.
-- Usage:
--   psql ... -v slug_a='demo-alpha' -v slug_b='demo-beta' -f link-companies.sql
--
-- Prints: community_id|slug_a|slug_b

\set ON_ERROR_STOP on

BEGIN;

WITH company_a AS (
  SELECT id FROM companies WHERE slug = :'slug_a'
),
company_b AS (
  SELECT id FROM companies WHERE slug = :'slug_b'
),
document_b AS (
  SELECT d.id
  FROM contact_documents d
  JOIN company_b b ON d.company_id = b.id
  WHERE d.is_default = true
  LIMIT 1
),
existing_community AS (
  SELECT cc.community_id
  FROM company_communities cc
  JOIN company_a a ON cc.company_id = a.id
  JOIN company_communities cc2 ON cc2.community_id = cc.community_id
  JOIN company_b b ON cc2.company_id = b.id
  LIMIT 1
),
created_community AS (
  INSERT INTO communities (
    sponsor_company_id, partner_group_id, inherit_community_id, name, description,
    short_description, type, terms, status, default_relationship, custom_sla,
    p1_message, p1_response_time, p2_message, p2_response_time, p3_message, p3_response_time,
    addendum, addendum_name, msteams_integration, msteams_manager_notified,
    created_at, updated_at
  )
  SELECT
    a.id,
    1,
    1,
    :'slug_a' || ' <> ' || :'slug_b' || ' demo community',
    'Demo community link',
    'Demo community link',
    'solution_support',
    'Demo terms',
    'active',
    'many_to_many',
    false,
    'P1', 1, 'P2', 2, 'P3', 3,
    '', 'Demo addendum', false, false,
    NOW(), NOW()
  FROM company_a a, company_b b
  WHERE NOT EXISTS (SELECT 1 FROM existing_community)
  RETURNING id
),
community_row AS (
  SELECT community_id AS id FROM existing_community
  UNION ALL
  SELECT id FROM created_community
  LIMIT 1
),
membership_a AS (
  INSERT INTO company_communities (
    company_id, community_id, is_sponsor, user_access_count, user_access_flag,
    member_relationship, is_active, created_at, updated_at
  )
  SELECT
    a.id, c.id, true, 10, 'ALL', 'many_to_many', true, NOW(), NOW()
  FROM company_a a
  CROSS JOIN community_row c
  WHERE NOT EXISTS (
    SELECT 1 FROM company_communities cc
    WHERE cc.company_id = a.id AND cc.community_id = c.id
  )
  RETURNING community_id
),
membership_b AS (
  INSERT INTO company_communities (
    company_id, community_id, document_id, is_sponsor, user_access_count, user_access_flag,
    member_relationship, is_active, created_at, updated_at
  )
  SELECT
    b.id, c.id, db.id, false, 10, 'ALL', 'many_to_many', true, NOW(), NOW()
  FROM company_b b
  CROSS JOIN community_row c
  CROSS JOIN document_b db
  WHERE NOT EXISTS (
    SELECT 1 FROM company_communities cc
    WHERE cc.company_id = b.id AND cc.community_id = c.id
  )
  RETURNING community_id
)
SELECT c.id || '|' || :'slug_a' || '|' || :'slug_b'
FROM community_row c;

COMMIT;
