insert into roles (id, name) values
    (1, 'ADMIN'),
    (2, 'CUSTOMER'),
    (3, 'SUPPORT'),
    (4, 'COMPLIANCE'),
    (5, 'AUDITOR')
on conflict (id) do nothing;

insert into role_permissions (role_id, permissions) values
    (1, 'MANAGE_USERS'), (1, 'MANAGE_ROLES'), (1, 'VIEW_AUDIT'),
    (2, 'VIEW_ACCOUNTS'), (2, 'INITIATE_PAYMENTS'), (2, 'VIEW_NOTIFICATIONS'),
    (3, 'VIEW_CUSTOMERS'), (3, 'VIEW_ACCOUNTS'), (3, 'MANAGE_ACCOUNTS'),
    (4, 'VIEW_CUSTOMERS'), (4, 'MANAGE_KYC'), (4, 'APPROVE_PAYMENTS'), (4, 'VIEW_AUDIT'),
    (5, 'VIEW_AUDIT')
on conflict do nothing;

insert into user_account (id, username, email, password_hash, status, mfa_enabled) values
    (1, 'admin', 'admin@bank.example', '{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', true),
    (2, 'customer', 'customer@example.com', '{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', true),
    (3, 'support', 'support@bank.example', '{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', true),
    (4, 'compliance', 'compliance@bank.example', '{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', true)
on conflict (id) do nothing;

insert into user_roles (user_id, role_id) values
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 4)
on conflict do nothing;

select setval(pg_get_serial_sequence('roles', 'id'), (select coalesce(max(id), 1) from roles));
select setval(pg_get_serial_sequence('user_account', 'id'), (select coalesce(max(id), 1) from user_account));
