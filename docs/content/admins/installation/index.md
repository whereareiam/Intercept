---
title: Installation
description: Install Intercept on Paper, Velocity or NeoForge.
---

## What you need

| Platform | Version | Also install |
| --- | --- | --- |
| Paper | 26.3 | [PacketEvents](https://modrinth.com/plugin/packetevents) 2.14 or newer |
| Velocity | 4.3 | [PacketEvents](https://modrinth.com/plugin/packetevents), to translate messages that come from the servers behind it |
| NeoForge | 21.1 for Minecraft 1.21.1 | nothing |

Paper and Velocity need Java 25, NeoForge Java 21.

## Install

1. Put the jar for your platform into `plugins/` (Paper, Velocity) or `mods/` (NeoForge). The bundle jar works on
   both Paper and Velocity.
2. Start the server once. Intercept writes its files into `plugins/Intercept/` on Paper, `plugins/intercept/` on
   Velocity and `intercept/` on NeoForge, and stores in a SQLite file beside them.
3. Stop the server, list the languages players may choose in `settings.yml`, and start it again.

```yaml
# settings.yml
defaultLanguage: en
languages: [en, de, ru]
```

On Paper and Velocity, Intercept downloads the libraries it needs into `.libraries/` on the first start, so that
start needs internet access.

## Check that it works

Join and run `/language de` (or another language from your list). Intercept answers in that language. Your client
does not need to change its own language.

## Several servers

One server is ready now. For a network, where every server shares the same messages and players' choices, follow
[Several servers](../guides/network/index.md) before you install Intercept on the next one.
