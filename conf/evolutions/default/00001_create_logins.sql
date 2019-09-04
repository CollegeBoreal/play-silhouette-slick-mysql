# logins schema

# --- !Ups
CREATE TABLE IF NOT EXISTS `LOGINS` (
  `providerId` VARCHAR(45) NULL,
  `providerKey` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`providerKey`)
  );
# --- !Downs

DROP TABLE `LOGINS`;
