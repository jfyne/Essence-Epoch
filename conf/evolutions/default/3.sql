
# --- !Ups

ALTER TABLE `epoch_users`
    ALTER `user_id` DROP DEFAULT;
ALTER TABLE `epoch_users`
    CHANGE COLUMN `user_id` `user_id` VARCHAR(100) NOT NULL FIRST;

# --- !Downs

ALTER TABLE `epoch_users`
    ALTER `user_id` DROP DEFAULT;
ALTER TABLE `epoch_users`
    CHANGE COLUMN `user_id` `user_id` BIGINT(11) NOT NULL FIRST;
