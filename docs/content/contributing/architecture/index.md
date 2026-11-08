---
title: Architecture
description: Intercept's modules, what each owns and which way they depend.
---

```text
intercept-api                     contracts several families share: languages, the catalog, shared messages,
                                  persistence and replication ports, lifecycle, command sources
intercept-common                  the catalog (one Semantica), shared messages and their folders, languages
intercept-interception/
  interception-api                surfaces, the interceptor, interception.yml
  interception-common             markers and rules
  interception                    wiring
  interception-library/
    common                        what every packet library does with an outgoing message
    library-packetevents          Paper and Velocity
    library-mixin                 NeoForge: a Mixin on the game's own packet sending
intercept-adapter-command         /language and /intercept, through Cloud and Commandant
intercept-adapter-persistence/    the database: Dialectica entities, Strata schema, Jdbi
intercept-adapter-replication/    Redis
intercept-integration/
  integration-placeholderapi      %intercept_language%
intercept-platform/
  common                          what a platform provides, and the injector
  bundle                          Paper and Velocity in one jar
  type/platform-paper|velocity|neoforge
intercept-testing                 Anvil journeys
```

Every family's root (`interception`, `persistence`, `replication`) is its wiring and the module others depend on;
its `-api` holds contracts and its `-common` the logic.

## The catalog

One Semantica per instance, with four layers, lowest first: Intercept's own texts from the jar, the remote
bundles over HTTP, the shared layer from the database, and `local/messages/`. Its translator is registered in
Adventure's global translator as semantica's authority: every other semantica translator yields the keys the
authority has, because Adventure keeps its translators in an unordered set.

## Publishing

`messages/` and `rules/` are a working copy of the shared layer; `cache/shared/` holds the same revision untouched.
`/intercept publish` sends the difference to the database in one transaction that raises the revision, then
replicates the revision. Every instance loads a newer revision when Redis announces it or polling finds it, writes
it to both copies and keeps unpublished edits on top.

## Languages on each platform

Paper: PacketEvents rewrites the locale of the client's settings and replays them when a choice changes. Velocity:
the effective locale. NeoForge: the Mixin renders every outgoing message in the chosen language.

## NeoForge packaging

NeoForge has no Attache, so the mod carries its libraries, relocated below `me.whereareiam.intercept.library`.
Adventure and semantica's core and Adventure format are nested jars, so every mod shares one copy of Adventure's
global translator and of `TextArgument`.
