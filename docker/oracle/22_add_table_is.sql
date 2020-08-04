CONNECT system/oracle;
-- Create starter set
CREATE TABLE IS_USER.subscriber (
         ACCOUNT_NUMBER VARCHAR2(15) PRIMARY KEY,
         msisdn VARCHAR2(11) NOT NULL);
COMMIT;
exit;
