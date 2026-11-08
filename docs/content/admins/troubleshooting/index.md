---
title: Troubleshooting
description: Symptoms, their causes, and what to check.
---

## A player sees a key, such as `essentials.motd`, instead of a text

No layer has the key in any language. Check the file's path: `translations/en/essentials.yml` with `motd:` gives
`essentials.motd`, while `translations/essentials.yml`, directly in the folder, is not read. A file must sit inside a
language folder.

## A published text does not appear on another server

- Check that both servers use the same database in `persistence.yml`.
- Without Redis, wait for `revisionPoll`.
- The other server may have the key in `translations/local/`, which wins over the network's text there.

## `/language` says a language is not available

Add it to `languages` in `settings.yml` and restart.

## Marked messages are not translated on Paper or Velocity

PacketEvents must be installed: Paper refuses to start Intercept without it, and Velocity starts Intercept without
interception. Check that the kind of message is on in `interception.yml` and that the marker matches its `marker`.

## A warning such as `markup de intercept.language.chosen ... lacks [gray, white]`

A translation uses other formatting tags than the English text. It is shown as written; the warning helps
translators keep messages alike.
