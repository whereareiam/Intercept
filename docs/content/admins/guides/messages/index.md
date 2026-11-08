---
title: Changing messages
description: Change a text for the whole network or for one server.
---

Every message has a key, such as `essentials.motd`, and a text for each language. Intercept looks a key up in
these places, the first that has it wins:

1. `local/messages/` of this server
2. the network's shared messages, in `messages/` on every server
3. the remote bundles, when [`remote`](../../../configuration/settings/index.mdx) is turned on in `settings.yml`
4. the plugin that sends the message, with its own texts

So a text you change in Intercept's folders wins over the one a plugin ships, and a change for one server wins over
the network's. In each place, a text in the player's language wins over one in the default language, which wins over
one in `all/`, the folder for every language.

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
  all/
    identica.yml          the same text for every language; see below
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

## Link a plugin's keys to your own keys

Translators should see your network's own keys, such as `game.account.name-taken`, rather than the keys of the
plugins you run. Write each plugin key once in `messages/all/`, with a text made only of
[references](#files-and-keys) to your keys:

```yaml
# messages/all/identica.yml
username:
  taken: "{@game.account.name-taken player=name}"
login:
  failed: "{@server.format.prefix}{@game.account.login-failed}"
```

A message in `all/` counts for every language, and its references are looked up in the player's language: a German
player who hits a taken name sees the German text of `game.account.name-taken`. `player=name` passes the plugin's
argument `name` on as `player`, so your texts can use their own argument names. Several plugin keys can link to the
same key of yours, and one key can combine several of yours.

Your keys' texts come from the [remote bundles](../../../configuration/settings/index.mdx) or from
`messages/<language>/`. Publish `all/` like any other change; a plugin key you give a real text in one language, in
`messages/de/` say, uses that text for German instead of the link.

## Undo a change

Delete the line from `messages/` and run `/intercept publish`; the network shows the remote bundles' or the
plugin's text again. For a local change, delete it from `local/messages/` and run `/intercept reload`.
