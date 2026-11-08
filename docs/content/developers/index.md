---
title: Sending translatable messages
description: How a plugin sends messages that Intercept and the network can translate.
---

Your plugin does not depend on Intercept. It owns its texts through semantica, registers a translator for them in
Adventure's global translator and sends keys. Without Intercept, players see your texts; with Intercept, the
network's texts win for every key the network has, in the language each player chose.

## Send keys with named arguments

```java
import me.whereareiam.semantica.format.adventure.v5.TextArgument;
import net.kyori.adventure.text.Component;

player.sendMessage(Component.translatable(
        "shop.bought",
        TextArgument.of("item", itemName),
        TextArgument.of("count", 3)
));
```

`shop.bought` might read `You bought {count, plural, one {# {item}} other {# {item}s}}.`. Name every argument; the
network's translators see the names.

## Register your own texts

```java
import me.whereareiam.semantica.catalog.Layer;
import me.whereareiam.semantica.common.Semantica;
import me.whereareiam.semantica.format.adventure.TagPolicy;
import me.whereareiam.semantica.format.adventure.v5.AdventureRenderer;
import me.whereareiam.semantica.format.adventure.v5.AdventureTranslator;
import me.whereareiam.semantica.source.file.FileSource;
import me.whereareiam.semantica.syntax.icu.IcuSyntax;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;

AdventureRenderer renderer = AdventureRenderer.of(TagPolicy.defaults());
Semantica semantica = Semantica.builder()
        .syntax(new IcuSyntax())
        .defaultLocale(Locale.ENGLISH)
        .layer(Layer.of(FileSource.of(dataFolder.resolve("messages"), configura)))
        .check(renderer.check())
        .build();

GlobalTranslator.translator().addSource(AdventureTranslator.of(semantica, Key.key("shop", "texts"), renderer));
```

Adventure asks its translators in no particular order. Intercept registers as the authority, and every semantica
translator leaves a key to it whenever the network has that key, so registering your translator before or after
Intercept makes no difference.

## When you cannot send keys

If a plugin can only send text, it can still ask for a key with a marker, such as
`<lang key="shop.bought" item="Sword" count=3>`. Admins can also translate finished text with rules; see
[Translating other plugins](../admins/guides/interception/index.md).
