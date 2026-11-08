---
title: Building
description: Build the plugin jars and the NeoForge mod.
---

You need JDK 21 and JDK 25; Gradle finds them in the usual places. Paper and Velocity are built on Java 25,
everything else on Java 21.

```bash
./gradlew pluginJars
```

`build/libs/` then holds `Intercept-<version>-PAPER.jar`, `-VELOCITY.jar`, `-BUNDLE.jar` (Paper and Velocity in
one) and `-NEOFORGE.jar`. `./gradlew assembleDistribution` collects them in `build/distribution/` under their release
names.

While semantica has no release, Intercept builds against `me.whereareiam.semantica:*:dev` from your local Maven
repository: build semantica's branch with `./gradlew publishToMavenLocal` first.

Run the unit tests with `./gradlew test`. Tests tagged `integration` start containers and run in
`./gradlew integrationTest`.
