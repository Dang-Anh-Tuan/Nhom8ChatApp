CREATE SCHEMA `chatapp` ;

CREATE TABLE `chatapp`.`user` (
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`username`),
  UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE);

CREATE TABLE `chatapp`.`message`  (
  `id` INT NOT NULL AUTO_INCREMENT  PRIMARY KEY,
  `msg` VARCHAR(255) NOT NULL
);

CREATE TABLE `chatapp`.`user_message`  (
  `id` INT NOT NULL AUTO_INCREMENT  PRIMARY KEY,
  `user_from` VARCHAR(50),
   `user_to` VARCHAR(50),
  `msg_id` INT,
  FOREIGN KEY (msg_id) REFERENCES message(id),
  FOREIGN KEY (user_from) REFERENCES user(username),
  FOREIGN KEY (user_to) REFERENCES user(username)
);

INSERT INTO `chatapp`.`user` (`username`, `password`) VALUES ('demo1', '123456');
INSERT INTO `chatapp`.`user` (`username`, `password`) VALUES ('demo12', '123456');
INSERT INTO `chatapp`.`user` (`username`, `password`) VALUES ('demo123', '123456');

