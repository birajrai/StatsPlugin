# StatsReloaded

## Database Schema Upgrade

If you are upgrading from an older version and encounter errors about missing tables or columns (such as `stats_pvp_kill_streak` or `timestamp` in `stats_last_join`/`stats_last_quit`), run the following SQL statements in your MySQL database:

```
CREATE TABLE IF NOT EXISTS stats_pvp_kill_streak (
    player_uuid VARCHAR(36) NOT NULL,
    world_uuid VARCHAR(36) NOT NULL,
    amount DOUBLE NOT NULL,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (player_uuid, world_uuid),
    FOREIGN KEY (player_uuid) REFERENCES stats_players(uuid) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (world_uuid) REFERENCES stats_worlds(uuid) ON DELETE RESTRICT ON UPDATE CASCADE
);

ALTER TABLE stats_last_join ADD COLUMN IF NOT EXISTS `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE stats_last_quit ADD COLUMN IF NOT EXISTS `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
```

If your MySQL version does not support `ADD COLUMN IF NOT EXISTS`, you may need to check for the column manually or use a tool like phpMyAdmin to add it.
