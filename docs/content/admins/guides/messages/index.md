---
title: Changing messages
description: Change a text for the whole network or for one server.
---

Every message has a key, such as `essentials.motd`, and a text for each language. Intercept looks a key up in
these places, the first that has it wins:

1. `local/messages/<language>/` of this server
2. the network's shared messages, in `messages/<language>/` on every server
3. the remote bundles, when [`remote`](../../../configuration/settings/index.mdx) is turned on in `settings.yml`
4. the plugin that sends the message, with its own texts

So a text you change in Intercept's folders wins over the one a plugin ships, and a change for one server wins over
the network's.

## Files and keys

A file's path inside the language folder is the beginning of its keys. Name files after the plugin whose messages
they hold, and keep texts that several plugins share in common files:

```text
plugins/Intercept/messages/
  en/
    server/format.yml     prefix: "<gold>Server</gold> » "     -> server.format.prefix
    essentials.yml        motd: "{@server.format.prefix}Hello!" -> essentials.motd
  de/
    essentials.yml
```

`{@server.format.prefix}` puts another key's text into this one, so a change of the prefix reaches every message
that uses it.

## Change a text for the network

1. On any server, open or create the file in `messages/<language>/` and change or add the text.
2. Run `/intercept publish`.

Intercept stores the change with your name and the time, and every server of the network shows it within a few
seconds. Only what you changed is stored: a text you set back to the remote bundles' is removed from the network's
changes.

`messages/` on every server always shows what the network has. When another server publishes while you are
editing, your server rewrites `messages/` with the network's new texts and keeps your unpublished changes on top.

## Change a text for one server

Put the text into `local/messages/<language>/` and run `/intercept reload`. Nothing in `local/` is published, and it
wins over the network's text on this server only. Use it for the exception, such as a different welcome on an event
server.

## Undo a change

Delete the line from `messages/` and run `/intercept publish`; the network shows the remote bundles' or the
plugin's text again. For a local change, delete it from `local/messages/` and run `/intercept reload`.
