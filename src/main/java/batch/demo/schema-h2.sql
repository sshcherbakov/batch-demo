DROP TABLE IF EXISTS DRAWING;
CREATE TABLE DRAWING (ID BIGINT NOT NULL AUTO_INCREMENT, 
         DATE DATE,
         NUMBERS VARCHAR(20),
         ZZ  INT,
         S INT,
         SPIEL77 VARCHAR(7),
         SUPER6 VARCHAR(6),
         STAKE DECIMAL(18,2),
         PRIMARY KEY (ID)
);