-- ALTER SESSION SET CONTAINER=TEST;
CREATE USER IS_USER IDENTIFIED BY is_user;
GRANT CONNECT, RESOURCE TO IS_USER;
ALTER USER IS_USER QUOTA UNLIMITED ON USERS;
exit;