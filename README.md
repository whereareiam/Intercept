# Intercept

> Intercept watches the components your server emits (chat, action bar, kick screens, and more) and routes them through
> a translation-aware pipeline with tag and regex interception.

- [Features](#features)
- [Developer](#developer)
- [Platform coverage](#platform-coverage)
- [Credits](#credits)

## Features

- Tag-based interception with configurable delimiters (default `<lang>`)
- Optional regex interception with safety limits and caching
- Per-component toggles for chat, action bar, and kick/disconnect
- Command suite for reload, inspection, locale switching, and DB sync
- Optional persistence via PostgreSQL/MariaDB with HikariCP tuning

## Use cases

- Ship a fully multilingual server experience even when upstream plugins only ship one locale.
- Translate plugins that expose no message configuration by intercepting their outbound components.
- Layer consistent formatting/prefixing across mixed plugin ecosystems without forking them.

## Platform coverage

- [x] Paper 1.20+:
    - [x] PacketEvents:
        - [x] Action bar
        - [x] Chat/Commands
        - [x] Kick/Disconnect
        - [ ] Boss bars
        - [ ] Entity: Armor stand
        - [ ] Entity: Display
        - [ ] Inventories
        - [ ] Motd
        - [ ] Scoreboard
        - [ ] Tab list
        - [ ] Tags
        - [ ] Titles
    - [ ] ProtocolLib:
        - [ ] Action bar
        - [ ] Chat/Commands
        - [ ] Kick/Disconnect
        - [ ] Boss bars
        - [ ] Entity: Armor stand
        - [ ] Entity: Display
        - [ ] Inventories
        - [ ] Motd
        - [ ] Scoreboard
        - [ ] Tab list
        - [ ] Tags
        - [ ] Titles
- [ ] Bukkit (legacy):
    - [ ] PacketEvents:
        - [ ] Action bar
        - [ ] Chat/Commands
        - [ ] Kick/Disconnect
        - [ ] Boss bars
        - [ ] Entity: Armor stand
        - [ ] Entity: Display
        - [ ] Inventories
        - [ ] Motd
        - [ ] Scoreboard
        - [ ] Tab list
        - [ ] Tags
        - [ ] Titles
    - [ ] ProtocolLib:
        - [ ] Action bar
        - [ ] Chat/Commands
        - [ ] Kick/Disconnect
        - [ ] Boss bars
        - [ ] Entity: Armor stand
        - [ ] Entity: Display
        - [ ] Inventories
        - [ ] Motd
        - [ ] Scoreboard
        - [ ] Tab list
        - [ ] Tags
        - [ ] Titles
- [ ] Velocity:
    - [ ] PacketEvents:
        - [ ] Action bar
        - [ ] Chat/Commands
        - [ ] Kick/Disconnect
        - [ ] Boss bars
        - [ ] Motd
        - [ ] Scoreboard
        - [ ] Tab list
        - [ ] Titles

## Developer

Add the API as `compileOnly` (runtime is provided by the platform jar).

<details>
<summary>Gradle (Kotlin DSL)</summary>

```kotlin
repositories {
    maven("https://maven.whereareiam.me/release")
    maven("https://maven.whereareiam.me/development")
}

dependencies {
    compileOnly("me.whereareiam:intercept-api:<version>")
}
```

</details>

<details>
<summary>Gradle (Groovy)</summary>

```groovy
repositories {
    maven { url "https://maven.whereareiam.me/release" }
    maven { url "https://maven.whereareiam.me/development" }
}

dependencies {
    compileOnly "me.whereareiam:intercept-api:<version>"
}
```

</details>

<details>
<summary>Maven</summary>

```xml

<repositories>
    <repository>
        <id>whereareiam-release</id>
        <url>https://maven.whereareiam.me/release</url>
    </repository>
    <repository>
        <id>whereareiam-development</id>
        <url>https://maven.whereareiam.me/development</url>
    </repository>
</repositories>

<dependencies>
<dependency>
    <groupId>me.whereareiam</groupId>
    <artifactId>intercept-api</artifactId>
    <version>&lt;version&gt;</version>
    <scope>provided</scope>
</dependency>
</dependencies>
```

</details>

## Credits

- Special thanks to the **Triton** project for blazing the trail on component interception; Intercept builds on those
  ideas to cover use cases Triton didn’t target.
