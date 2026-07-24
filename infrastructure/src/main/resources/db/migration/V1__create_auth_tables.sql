CREATE TABLE m_administrators (
  id CHAR(36) NOT NULL PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE m_roles (
  id CHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(255),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE m_permissions (
  id CHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(255),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE m_role_permissions (
  role_id CHAR(36) NOT NULL,
  permission_id CHAR(36) NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  FOREIGN KEY (role_id) REFERENCES m_roles(id),
  FOREIGN KEY (permission_id) REFERENCES m_permissions(id)
);

CREATE TABLE m_administrator_roles (
  administrator_id CHAR(36) NOT NULL,
  role_id CHAR(36) NOT NULL,
  PRIMARY KEY (administrator_id, role_id),
  FOREIGN KEY (administrator_id) REFERENCES m_administrators(id),
  FOREIGN KEY (role_id) REFERENCES m_roles(id)
);

INSERT INTO m_roles (id, name, description, created_at, updated_at, version)
VALUES (UUID(), 'ADMIN', 'Default role assigned on administrator self-registration.', NOW(), NOW(), 0);
