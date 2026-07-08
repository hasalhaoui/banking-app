UPDATE user_account
SET password_hash = '{bcrypt}$2a$10$82FK7M2mog4DJvCxSi0TwOU8daZzuU1VMRB64oRGeSRiaN2y84VaW',
    status = 'ACTIVE',
    failed_login_attempts = 0
WHERE username IN ('admin', 'customer', 'support', 'compliance');