ALTER TABLE sys_user
    ADD COLUMN password_updated_at TIMESTAMPTZ,
    ADD COLUMN last_login_at TIMESTAMPTZ,
    ADD COLUMN last_login_ip VARCHAR(64),
    ADD COLUMN account_locked_at TIMESTAMPTZ,
    ADD COLUMN disabled_reason VARCHAR(255);

UPDATE sys_user
SET password_updated_at = COALESCE(updated_at, created_at, CURRENT_TIMESTAMP)
WHERE password_updated_at IS NULL;

ALTER TABLE sys_user
    ALTER COLUMN password_updated_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN password_updated_at SET NOT NULL;

ALTER TABLE sys_user
    ADD CONSTRAINT ck_sys_user_status
        CHECK (user_status IN ('ACTIVE', 'DISABLED', 'LOCKED', 'DELETED'));

CREATE INDEX idx_sys_user_status ON sys_user (user_status);

ALTER TABLE sys_role
    ADD COLUMN role_type VARCHAR(32),
    ADD COLUMN is_builtin BOOLEAN,
    ADD COLUMN sort_order INTEGER,
    ADD COLUMN disabled_reason VARCHAR(255);

UPDATE sys_role
SET role_type = 'SYSTEM',
    is_builtin = TRUE,
    sort_order = CASE role_code
        WHEN 'ROLE_ADMIN' THEN 10
        WHEN 'ROLE_USER' THEN 20
        ELSE 100
    END
WHERE role_type IS NULL
   OR is_builtin IS NULL
   OR sort_order IS NULL;

ALTER TABLE sys_role
    ALTER COLUMN role_type SET DEFAULT 'SYSTEM',
    ALTER COLUMN role_type SET NOT NULL,
    ALTER COLUMN is_builtin SET DEFAULT FALSE,
    ALTER COLUMN is_builtin SET NOT NULL,
    ALTER COLUMN sort_order SET DEFAULT 100,
    ALTER COLUMN sort_order SET NOT NULL;

ALTER TABLE sys_role
    ADD CONSTRAINT ck_sys_role_status
        CHECK (role_status IN ('ACTIVE', 'DISABLED')),
    ADD CONSTRAINT ck_sys_role_type
        CHECK (role_type IN ('SYSTEM', 'EXTENSION', 'RESERVED'));

CREATE INDEX idx_sys_role_status ON sys_role (role_status);
CREATE INDEX idx_sys_role_type ON sys_role (role_type);

ALTER TABLE sys_user_role
    ADD COLUMN assignment_status VARCHAR(32),
    ADD COLUMN expires_at TIMESTAMPTZ,
    ADD COLUMN updated_at TIMESTAMPTZ,
    ADD COLUMN updated_by BIGINT,
    ADD COLUMN remark VARCHAR(255);

UPDATE sys_user_role
SET assignment_status = 'ACTIVE',
    updated_at = COALESCE(created_at, CURRENT_TIMESTAMP)
WHERE assignment_status IS NULL
   OR updated_at IS NULL;

ALTER TABLE sys_user_role
    ALTER COLUMN assignment_status SET DEFAULT 'ACTIVE',
    ALTER COLUMN assignment_status SET NOT NULL,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE sys_user_role
    ADD CONSTRAINT ck_sys_user_role_status
        CHECK (assignment_status IN ('ACTIVE', 'REVOKED', 'EXPIRED'));

CREATE INDEX idx_sys_user_role_role_id ON sys_user_role (role_id);
CREATE INDEX idx_sys_user_role_status ON sys_user_role (assignment_status);

INSERT INTO sys_role (
    role_code,
    role_name,
    role_status,
    role_type,
    is_builtin,
    sort_order,
    remark
)
SELECT
    'ROLE_MENTOR',
    '导师',
    'DISABLED',
    'RESERVED',
    TRUE,
    30,
    '为后续导师角色扩展预留'
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_role
    WHERE role_code = 'ROLE_MENTOR'
);
