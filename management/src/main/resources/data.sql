-- Seed Default Users
-- admin / admin123 (BCrypt: $2a$10$8.UnVuG9HHgffUDAlk8GP.3q3gOP3h0z05m.V.vW6g2H67mGj6K2C)
-- recruiter / recruiter123 (BCrypt: $2a$10$e0myzXyZJp6yX.j9fH2YQ.fX6mE0kQ/Zq4D5mK2rC0i1G2y3M4m5a)
INSERT IGNORE INTO users (username, password, role)
SELECT 'admin', '$2a$10$8.UnVuG9HHgffUDAlk8GP.3q3gOP3h0z05m.V.vW6g2H67mGj6K2C', 'ADMIN'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT IGNORE INTO users (username, password, role)
SELECT 'recruiter', '$2a$10$e0myzXyZJp6yX.j9fH2YQ.fX6mE0kQ/Zq4D5mK2rC0i1G2y3M4m5a', 'RECRUITER'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'recruiter');
