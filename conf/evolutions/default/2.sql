
# --- !Ups

ALTER TABLE `epoch_users`
    ADD COLUMN `user_role` VARCHAR(50) NOT NULL DEFAULT 'user' AFTER `user_token`;

# --- !Downs

ALTER TABLE `epoch_users`
    DROP COLUMN `user_role`
