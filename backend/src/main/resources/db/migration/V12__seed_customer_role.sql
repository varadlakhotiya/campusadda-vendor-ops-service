INSERT INTO roles (role_code, role_name, description)
SELECT 'CUSTOMER', 'Customer', 'Customer using CampusAdda ordering portal'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE role_code = 'CUSTOMER'
);