---
title: Reading players' languages
description: Ask the running Intercept which language a player chose, on Paper, Velocity and NeoForge.
---

Intercept stores the language each player chose for the whole network. Another plugin or mod reads that choice
through `InterceptAPI`, the same way on Paper, Velocity and NeoForge, for example to render its own texts in it.

## Add the dependency

Intercept's API, `me.whereareiam.intercept:api`, is published to the
[public Maven registry](https://registry.whereareiam.me/maven/packages). Compile against it and leave it out of your
jar: Intercept carries its API under the same names on every platform, so a relocated copy in your jar would never
meet the running Intercept.

```kotlin
repositories {
    maven("https://registry.whereareiam.me/maven/packages")
}

dependencies {
    compileOnly("me.whereareiam.intercept:api:1.0.0")
}
```

If your plugin shades its dependencies, keep `me.whereareiam.intercept` out of its relocations.

On Paper, declare Intercept as a dependency so your plugin sees its classes: `softdepend: [Intercept]` in
`plugin.yml`, or an entry under `dependencies.server` with `join-classpath: true` in `paper-plugin.yml`. Velocity and
NeoForge share the classes of every plugin and mod.

## Get the API

Intercept registers its API as it starts, before it intercepts messages and handles players, and removes it when
it stops.

```java
import me.whereareiam.intercept.InterceptAPI;

Locale language = InterceptAPI.find()
        .flatMap(intercept -> intercept.languages().chosen(player.getUniqueId()))
        .orElse(clientLocale);
```

- `InterceptAPI.find()` returns the API, or empty when Intercept is not running; use it when Intercept is optional.
- `InterceptAPI.get()` returns the API and throws when Intercept is not running.
- `InterceptAPI.whenReady(intercept -> ...)` runs once Intercept is ready, as it begins handling players, or at once
  when it already is; use it when your plugin may start before Intercept.

Pass the `InterceptAPI` to the classes that need it instead of calling the static methods everywhere, so they can be
given a mock in tests.

## Read a player's language

`languages()` returns the `LanguageService`:

| Method | Returns |
| --- | --- |
| `chosen(uniqueId)` | the language the player chose, or empty when they follow their client |
| `effective(uniqueId, client)` | the player's choice, or else the language their client reports |
| `available()` | the languages players may choose, as the admin configured them |

A choice is the same on every server of the network: Intercept replicates it, so a player keeps their language when
they switch servers.
