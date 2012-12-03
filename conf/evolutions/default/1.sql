# Users schema

# --- !Ups

CREATE TABLE `epoch_users` (
    `user_id` BIGINT NOT NULL,
    `user_email` VARCHAR(100) NOT NULL,
    `user_name` VARCHAR(100) NULL,
    `user_given` VARCHAR(100) NULL,
    `user_family` VARCHAR(100) NULL,
    `user_picture` VARCHAR(200) NULL,
    `user_token` VARCHAR(255) NULL DEFAULT NULL,
    `user_refresh` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`user_id`, `user_email`)
)
COLLATE='utf8_unicode_ci'
ENGINE=MyISAM;

# --- !Downs

DROP TABLE `epoch_users`;
