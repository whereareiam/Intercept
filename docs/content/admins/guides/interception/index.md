---
title: Translating other plugins
description: Translate system messages of plugins that only send text.
---

Plugins built for Intercept send keys, which Intercept translates. Older plugins send finished English text. For
those, Intercept looks at each system message on its way to the player, in chat, in the action bar and in the
reason of a kick, and translates it in two ways. Messages players write in chat are never changed.

## Markers

A plugin whose messages you can edit can ask for a key with a marker in its text:

```text
<lang key="essentials.motd" player="Alice">
```

Intercept replaces the marker with the text of `essentials.motd` in the player's language, with `{player}` filled
in. Values without spaces need no quotes: `<lang key=essentials.motd player=Alice>`. The marker's shape is set per
surface in `interception.yml`.

## Rules

For text you cannot edit, a rule recognises the message and names the key that replaces it. Put rules into
`rules/`, one file per plugin, and publish them like messages:

```yaml
# rules/essentials.yml
no-permission:
  pattern: "You don't have permission: ([\\w.]+)"
  key: essentials.permission.missing
  arguments:
    permission: 1
  surfaces: [chat, action-bar]
  priority: 10
  replace: match
```

- `pattern` is a regular expression matched against the message's plain text.
- `arguments` names the parts of the match: `1` is the first group, a name is a named group.
- `replace: match` replaces the matched part and keeps the rest; `whole` replaces the whole message.
- Rules with a higher `priority` are tried first; the first that matches wins. A message with a marker is not
  checked against rules.

A rule in `local/rules/` with the same file and name replaces the network's rule on that server.

See [rule files](../../../configuration/rules/index.mdx) for every field.
