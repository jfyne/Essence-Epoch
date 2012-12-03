# --- !Ups
ALTER TABLE `epoch_users`
    ADD COLUMN `user_calendar` VARCHAR(255) NULL AFTER `user_role`;
# --- !Downs
ALTER TABLE `epoch_users`
    DROP COLUMN `user_calendar`;
