# Intercept

Intercept is the translation database of a Minecraft network. It shows every message in the language each player
chose, from one set of texts: the plugins' own texts or bundles from an organisation's translations repository, the
network's changes to them, and the exceptions of one server. It runs on Paper, Velocity and NeoForge.

- Players choose their language once with `/language`, for every server.
- Admins change a text for the whole network with `/intercept publish`.
- Plugins that only send English text are translated through markers and rules.

See the [documentation](docs/content/admins/index.md) to install and configure it.

## Building

```bash
./gradlew pluginJars
```

Contributing starts at [building](docs/content/contributing/building/index.md).
