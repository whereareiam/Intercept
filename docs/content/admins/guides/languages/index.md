---
title: Languages
description: How players choose their language and how it applies on each platform.
---

A player sees the language of their game until they choose one with `/language <language>`. The choice is stored
for the whole network: it applies at once on the server they are on, follows them to every other server and is
still there when they come back. `/language reset` goes back to the game's language.

Players can choose the languages listed under `languages` in `settings.yml`. A player whose language has no text for
a message sees the text of `defaultLanguage`.

## On each platform

- **Paper**: the server sees the chosen language as the player's own, so every plugin that renders keys in the
  player's language follows the choice as well.
- **Velocity**: the proxy uses the chosen language for everything it renders. Servers behind it that run Intercept
  apply the choice themselves.
- **NeoForge**: Intercept renders each message in the chosen language when it is sent.

## Several servers

Choices are stored in the database. With [Redis](../network/index.md) turned on, a choice made on one server applies
on every other server the player is connected to at that moment; without it, it applies the next time they join one.
