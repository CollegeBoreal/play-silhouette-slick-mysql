# --- passwords schema

# --- !Ups

CREATE TABLE IF NOT EXISTS `PASSWORDS` (
  `password` VARCHAR(45) NOT NULL,
  `hasher` VARCHAR(45) NOT NULL,
  `secret` VARCHAR(255) NOT NULL,
  `salt` VARCHAR(45) NULL,
  PRIMARY KEY (`password`)
);

# --- !Downs

DROP TABLE `PASSWORDS`;
