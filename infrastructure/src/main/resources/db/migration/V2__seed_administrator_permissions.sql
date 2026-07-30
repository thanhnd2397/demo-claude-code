INSERT INTO m_permissions (id, name, description, created_at, updated_at, version)
VALUES
  (UUID(), 'ADMINISTRATOR_READ', 'Allows listing administrator accounts.', NOW(), NOW(), 0),
  (UUID(), 'ADMINISTRATOR_MANAGE', 'Allows updating administrator roles.', NOW(), NOW(), 0);

INSERT INTO m_role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM m_roles r
CROSS JOIN m_permissions p
WHERE r.name = 'ADMIN'
  AND p.name IN ('ADMINISTRATOR_READ', 'ADMINISTRATOR_MANAGE');
