ALTER TABLE `logistimo`.`INVNTRY` ADD UON DATETIME NULL;
CREATE TABLE `MATERIALMANUFACTURERS` (
  `KEY` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `MATERIAL_ID` BIGINT(20) NOT NULL,
  `MFR_CODE` BIGINT(20) NOT NULL,
  `MFR_NAME` VARCHAR(255) NOT NULL,
  `MATERIAL_CODE` BIGINT(20) NOT NULL,
  `QTY` FLOAT NULL,
  PRIMARY KEY (`KEY`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
UPDATE TRANSCATION SET TOT = "ts" WHERE TID IN ( SELECT ORDERID FROM `ORDER` WHERE OTY = 0);
UPDATE TRANSACTION SET TOT = "os" WHERE TOT = "s";

ALTER TABLE `logistimo`.`ORDER` ADD CVT DATETIME NULL, ADD VVT DATETIME NULL, ADD PART BIGINT(20) NULL, ADD SART BIGINT(20) NULL, ADD TART BIGINT(20) NULL;