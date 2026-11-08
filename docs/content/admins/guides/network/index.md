---
title: Several servers
description: Share messages, rules and language choices between servers and proxies.
---

On a network, every server and proxy that runs Intercept uses the same database. Redis is optional and makes
changes arrive at once.

## Set up

1. Create a PostgreSQL or MySQL database that every server can reach.
2. On every server, set it in `persistence.yml`:

   ```yaml
   type: POSTGRES
   host: db.internal
   port: 5432
   database: intercept
   username: intercept
   password: secret
   ```

3. Optionally, turn on Redis in `replication.yml` on every server, with the same `channel`:

   ```yaml
   enabled: true
   host: redis.internal
   port: 6379
   ```

4. Give each server its own `instance` name in `settings.yml`, so the log tells them apart.

## What is shared

| What | Without Redis | With Redis |
| --- | --- | --- |
| a published message or rule | every server finds it within `revisionPoll` (30 seconds by default) | at once |
| a player's language | the next time they join a server | at once, on every server they are on |
| `/intercept reload --everywhere` | only this server reloads | every server reloads |

Each server keeps working when Redis is down; it falls back to polling the database.

## Proxies and the servers behind them

Run Intercept on the proxy and on the servers behind it. The proxy translates what it sends itself and, with
PacketEvents, messages passing through it; each server translates its own. Both read the same choices and
messages from the database.
