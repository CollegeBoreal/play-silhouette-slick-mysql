# --- providers schema

# --- !Ups
CREATE TABLE IF NOT EXISTS `PROVIDERS` (
  `provider` VARCHAR(45) NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`provider`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC)
);

# --- !Downs

DROP TABLE `PROVIDERS`;
