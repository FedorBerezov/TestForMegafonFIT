CONNECT system/oracle;
-- Create starter set
CREATE TABLE SUS_USER.subscriber (
         msisdn VARCHAR2(11) PRIMARY KEY,
         ACCOUNT_NUMBER VARCHAR2(15),
         status VARCHAR2(50));
COMMIT;
exit;
