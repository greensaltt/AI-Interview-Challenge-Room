insert into sys_role (role_code, role_name, role_status, role_type, is_builtin, sort_order, remark)
values
    ('ROLE_ADMIN', '系统管理员', 'ACTIVE', 'SYSTEM', true, 10, '测试环境管理员角色'),
    ('ROLE_USER', '普通用户', 'ACTIVE', 'SYSTEM', true, 20, '测试环境普通用户角色'),
    ('ROLE_MENTOR', '导师', 'DISABLED', 'RESERVED', true, 30, '测试环境导师预留角色');
