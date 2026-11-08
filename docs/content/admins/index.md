---
title: Overview
description: What Intercept does on a network and where to start.
---

Intercept is the translation database of a Minecraft network. Every server and proxy it runs on shows each
message in the language the player chose, from one set of texts: the organisation's translations, the changes the
network made to them, and a few changes of one server.

It runs on Paper, on Velocity and on NeoForge for Minecraft 1.21.1.

## What players notice

- `/language de` switches every message to German, on every server of the network, and stays chosen after they
  leave. `/language reset` goes back to the language of their game.
- Messages of plugins that only send English text can be translated as well, so a player sees one language
  everywhere.

## What admins do

- Change a text for the whole network in `messages/` on any server and run `/intercept publish`. Every other server
  shows the new text within seconds.
- Change a text for one server only in `local/messages/`.
- Translate another plugin's English messages with rules in `rules/`.

## Where to start

| You want to | Read |
| --- | --- |
| install Intercept | [Installation](./installation/index.md) |
| change a text for the network or one server | [Changing messages](./guides/messages/index.md) |
| let players choose their language | [Languages](./guides/languages/index.md) |
| translate a plugin that sends only English | [Translating other plugins](./guides/interception/index.md) |
| run Intercept on several servers | [Several servers](./guides/network/index.md) |
| look up a setting | [Configuration](../configuration/index.mdx) |
| fix something that does not work | [Troubleshooting](./troubleshooting/index.md) |
