# --- users schema
# --- To avoid constraint name conflict
# --- constraint_XXX were added to the CONSTRAINT name
# --- generated by MySQLWorkBench EER Model

# --- !Ups

CREATE TABLE IF NOT EXISTS `USERS` (
  `user` INT NOT NULL AUTO_INCREMENT,
  `number` VARCHAR(45) NULL,
  `password` VARCHAR(45) NOT NULL,
  `active` INT NOT NULL,
  `created` DATETIME NOT NULL,
  PRIMARY KEY (`user`),
  UNIQUE INDEX `key_UNIQUE` (`password` ASC),
  CONSTRAINT `constraint_user_key`
    FOREIGN KEY (`password`)
    REFERENCES `PASSWORDS` (`password`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)

# --- !Downs

DROP TABLE `USERS`;