INSERT INTO USER_ROLE (ID, ROLE) VALUES (1, 'ROLE_ADMIN'),  (2, 'ROLE_USER'),  (3, 'ROLE_GUEST');
INSERT INTO OAUTH_CLIENT_DETAILS (CLIENT_ID, RESOURCE_IDS, CLIENT_SECRET, SCOPE, AUTHORIZED_GRANT_TYPES, WEB_SERVER_REDIRECT_URI, AUTHORITIES, ACCESS_TOKEN_VALIDITY, REFRESH_TOKEN_VALIDITY, ADDITIONAL_INFORMATION, AUTOAPPROVE) VALUES ('system', NULL, 'secret', 'read,write', 'password,refresh_token', 'https://localhost:8443/login', 'ROLE_USER', '14400', null, null, '');
INSERT INTO USERS(ID, NAME, EMAIL, ENABLED, PASSWORD) VALUES (1, 'Test', 'test@test.com', 't', '$2a$10$iJ9ZllxclX4WJ7.m0nCfDe0PhUSc22yDj30g1KRWGmqiqo3DQl.v2');
INSERT INTO USERS_USER_ROLE(USER_ID, ROLE_ID) VALUES (1,1), (1, 2);