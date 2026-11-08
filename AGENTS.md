# AGENTS.md

## Intercept At A Glance

Intercept is the **translation database of a Minecraft network**. It answers every translatable key from one catalog
(remote bundles, the network's shared messages, a server's local messages), in the language each player
chose, and translates system messages of plugins that only send text.

When changing behavior:

- keep plugins independent of Intercept: they send keys with named `TextArgument`s and register their own semantica
  translator; Intercept wins through semantica's authority, never through a dependency of theirs
- keep platform differences in the platform family and the packet library that feeds it
- keep changes in the module that owns the behavior

---

## Repository Orientation

### Core Module Map

- `intercept-api`
  Only what several families share: `LanguageService` and `LanguageApplier`, `TranslationService`, `MessageService`, the persistence ports `SharedStore` and `LanguageStore`, `ReplicationService` and its messages, `Lifecycle`, `CommandSource`, `LibraryLoader` and `settings.yml`. Consumers depend on the family api they need, never on one global api.
- `intercept-common`
  The catalog (one Semantica with the layers defaults, remote, shared, local, registered as semantica's authority in Adventure's `GlobalTranslator`), the shared messages and their folders (`translations/` with a folder per language and `all/` for the root locale, `translations/rules/`, `.cache/shared/`), polling and publishing, and players' languages.
- `intercept-interception/*`
  The interception family. `interception-api` holds `Surface` (chat, action bar, kick), `MessageInterceptor` and `interception.yml`; `interception-common` markers and rules; `interception` the wiring. `interception-library/common` is what every packet library does with an outgoing message; `library-packetevents` serves Paper and Velocity, `library-mixin` NeoForge through a Mixin on the game's packet sending.
- `intercept-adapter-command`
  `/language` and `/intercept publish|reload`, on Cloud with Commandant's definitions from `commands.yml`.
- `intercept-adapter-persistence/*`
  `persistence-api` (published: `DatabaseService`, `DatabaseSchema`, `persistence.yml`), `persistence-common` (connection, Dialectica entities, the Strata schema stream `intercept`, the stores), `persistence` (wiring). Tables are declared only with Dialectica; reads and writes go through `EntityAccess`, with hand-written Jdbi only for lists through the entity mapper and the revision's atomic increment. Intercept was never released, so its schema has no migrations yet.
- `intercept-adapter-replication/*`
  `replication-api` (published: `ReplicationTransport`, `replication.yml`), `replication-common` (Redis transport, the line codec, `TransportReplicationService`), `replication` (wiring).
- `intercept-integration/integration-placeholderapi`
  `%intercept_language%` on Paper.
- `intercept-platform/*`
  `common` holds `PlatformConfiguration` (what a platform provides) and `InterceptInjector`. `type/platform-paper`, `type/platform-velocity` and `type/platform-neoforge` are the platforms; `bundle` packs Paper and Velocity into one jar.
- `intercept-testing`
  Live journeys through Anvil on real Paper, Velocity and NeoForge networks. Not part of the shipped plugin.
- `build-logic/`
  `module-java` (Java 21, Lombok, common dependencies), `module-host` (Java 25 for Paper 26.3 and Velocity 4.3, the first hosts on Adventure 5), `module-api` (a published `-api` module), `packaging-publication`, `packaging-relocations`, `packaging-shaded-jar`, `packaging-platform-jar` and `packaging-bundle-jar`.
- `docs/content/`
  User-facing and developer-facing documentation.

### Where To Look First

- For what a family exposes, start in its `-api`; for contracts several families share, `intercept-api`.
- For how a key is looked up or what publishing does, start in `intercept-common` (`translation`, `message`).
- For a message that is or is not translated on its way to a player, start in `interception-common`, then the platform's packet library.
- For how a chosen language reaches the host, start in the platform's `LanguageApplier`.
- For tables, start in `persistence-common` (`entity`, `schema`, `store`).
- For build or packaging, check `build-logic/` and the platform's build file (NeoForge carries its libraries itself).

---

## Architecture Rules

- Keep responsibilities in the narrowest owning module; a family's root module only wires.
- Public API surfaces are `intercept-api` and every `*-api` module; they need complete Javadocs.
- Paper and Velocity load libraries through Attache. Intercept's classes are compiled against relocated Guice, Guava, Jackson, SnakeYAML and Jdbi, so every library declared through Attache that uses them is relocated the same way in its build file.
- A platform's entry class only names its host and Attache: the host verifies it before Attache has loaded Guice. Velocity builds it with its own Guice, so it is injected through `jakarta.inject`.
- NeoForge has no Attache: the mod shades and relocates its libraries, and nests Adventure, semantica's core, its Adventure format, ICU and Cloud as Jar-in-Jar, unrelocated, so mods share them.
- Never change signed player chat; interception handles system messages only.

---

## Runtime And Library Conventions

- Texts are semantica messages; commands answer with Intercept's keys rendered through Adventure's `GlobalTranslator` in the source's language.
- Reuse the owner's libraries in their areas: `configura` (configuration files), `semantica` (catalog and formats), `commandant` (command definitions), `dialectica` and `strata` (tables and their streams), `attache` (runtime libraries).
- Use Java 21 code; only Paper and Velocity modules compile for Java 25.
- Follow `gradle/libs.versions.toml`; check the latest version of a new dependency instead of relying on memory.
- semantica is built from its branch and published to the local Maven repository as `dev` until it is released.

---

## Build, Test, And Validation

- `./gradlew test`
- `./gradlew pluginJars`
- `./gradlew test assembleDistribution` (the quick part of the CI checks)
- `./gradlew integrationTest` (tests that start containers)
- `./gradlew :intercept-testing:anvilPaperTest`, `anvilVelocityTest`, `anvilNeoforgeTest` (journeys; they need Docker for PostgreSQL and Redis)

Live tests:

- each platform has a scenario of two instances on PostgreSQL and Redis containers: `PaperNetwork`, `VelocityNetwork`, `NeoForgeNetwork`
- a journey class carries its platform's tag (`paper`, `velocity`, `neoforge`), which is its suite
- `Journey` connects a player to one instance (the same name on two instances is one player), runs commands, waits for chat text, and runs console commands or writes files on an instance
- the Paper network has the probe, a plugin with its own semantica translator, to show the authority at work
- a failed journey keeps its network under `intercept-testing/build/anvil/`; read the `anvil-console.log` files there
- add or update a journey when a change affects what a player sees

See `docs/content/contributing/testing/`.

---

## Branching And Pull Requests

### Branch Flow

- Build each feature, fix, or isolated piece of work in its own `feature/...` branch.
- Merge feature work into `dev` through a pull request. Pull requests are merged by rebase: each commit lands on `dev` as written, so a branch holds clean conventional commits and no fixups.
- Releases are cut from `dev`: publishing the release draft tags the current `dev`.

### Pull Request Checks

- Every pull request gets the metadata check (title and label) and the checks: `test` and `assembleDistribution`, `integrationTest`, and one job per journey suite (`paper`, `velocity`, `neoforge`).
- The `dev` ruleset requires every one of these checks, so no pull request merges with a failing or missing check unless an administrator bypasses it.
- Dependabot groups minor and patch updates into one pull request per ecosystem, and `dependabot-auto-merge.yml` turns on auto-merge for it, so it merges once the checks pass; a major update gets a pull request of its own and waits for review. The merge goes through a GitHub App, because a merge by `GITHUB_TOKEN` would not start the workflows of the push to `dev`; its `MERGE_APP_CLIENT_ID` and `MERGE_APP_PRIVATE_KEY` are Dependabot secrets, since a run Dependabot starts gets no Actions secrets. When the checks gain or rename a job, update the required checks of the ruleset.
- Label a pull request with exactly one of `feature`, `change`, `bug`, `dependencies`, or with `skip-changelog`; `major` may accompany a category.
- A run keeps as artifacts only what someone downloads: `intercept-plugins-<version>` from a development build or release, and `test-results`, `test-results-integration` or `test-results-journeys-<suite>` when a test failed.
- Development builds are started manually and publish a branch-qualified version. A published GitHub release publishes Maven artifacts and attaches the distribution; a manual run of the release workflow only verifies the release again.
- Entry workflows only select a trigger and call `reusable-verify.yml` and `reusable-publish.yml`; shared steps come from `whereareiam/devops`.
- The Gradle build cache is on. Pull requests only read caches, so `build-cache.yml` compiles on every push to `dev` and writes the Gradle cache, and runs one journey of each suite so `whereareiam/anvil-actions/setup` saves the servers and workspaces the journey jobs restore; when the checks start running other tasks before their tests, add those tasks to its command.

### Issues

- A suggestion opens with `suggestion`, the label of an idea not decided on yet. An accepted one trades it for `feature` or `change`; a declined one is closed as not planned.
- A bug report opens with `bug`.

### Release Notes

A merged pull request lands in the release draft as `* <title> ([#<number>](<url>))`, under the category of its label. The draft is written for server admins and players, so the title and the label decide what they read.

#### Titles

- use the strict format `Area: Description`, with a past-tense description of what changed for the reader: `Introduced`, `Changed`, `Fixed`, `Removed`
- describe the effect, not the implementation: `Languages: Fixed a choice not reaching the proxy`, not `Replication: Published LanguageChosen from the backend`
- the draft sorts by the exact area, so spell an area exactly as listed below

Areas for admins and players:

`Commands`, `Configuration`, `Database`, `Interception`, `Languages`, `Messages`, `Platform`, `Replication`

- reuse an existing area whenever one fits; a new admin command is `Commands`, a new setting belongs to the area of its file
- add a new area only for something admins recognise by that name, such as a new surface or config file, and add it to this list, the issue template's areas and the pull request template in the same pull request
- never use module, package, or class names as an area: no `Engine`, `Pipeline`, `Capability`, `Adapter`, `Common`, `Config`

`API:` is for changes a plugin developer compiles against: the `-api` modules. The drafter moves `API:` changes out of their category into a closing "For developers" section. A change that matters to both admins and developers takes the admin area; mention the API part in the pull request body.

#### Labels

| Label | Use for | Draft section |
| --- | --- | --- |
| `feature` | something new for admins, players, or developers | What’s new |
| `change` | changed behavior, configuration, or API | Enhancements |
| `bug` | a fix for a bug that shipped in a release | Fixes |
| `dependencies` | a library update | Dependencies |
| `skip-changelog` | anything a reader of the release would not notice | not listed |

Use `skip-changelog` for:

- internal refactors with no effect on admins, players, or the public API
- build, CI, test, and documentation-only changes
- a fix for a bug that never shipped: when the broken code was introduced after the last release, the fix belongs to the work that introduced it, not to the release notes; compare against the last release tag (`git grep <symbol> <tag>`, `git log <tag>..HEAD -- <path>`) to decide
- changes to something that was itself added after the last release and never shipped

Add `major` next to the category when admins or developers must act on upgrade: configuration moved or reset without a migration, stored data that is not carried over, or an incompatible public API change. One `major` pull request makes the draft a major version.

#### Release notes file

The drafter rebuilds the draft on every push to `dev`, so text typed into the draft is lost. Everything written by hand, such as the subtitle, the summary, upgrade notes, and security callouts, lives in `.github/release-notes.md` and is changed through pull requests like any other file.

```markdown
---
version: 1.0.0
---

# Update 1.0.0 - One Network, Every Language

One or two sentences about the release.

## Upgrading

* Install fresh; earlier development builds are not supported.
```

- the content replaces the subtitle and summary placeholders of the draft only while `version` matches the version the draft resolves to; notes of a published release therefore never reach the next draft
- when a change needs a note, add it to this file in the same pull request; if the file names an already published version, start the notes of the next release by replacing the whole file and setting `version` to the version the draft shows
- keep the heading as `# Update <version> - <subtitle>`; the stores take the subtitle as the version name
- bullets in this file are not sorted into the changelog

#### Publishing a release

- fix the changelog part of the draft by editing titles and labels of merged pull requests, then rerun the `Release drafter` workflow
- the subtitle and summary come from `.github/release-notes.md`; the release workflow stops while a placeholder is left
- the summary names what admins must do on upgrade (everything labelled `major`) and calls out security fixes
- the release workflow attaches `release-notes-bbcode.txt` for stores without automatic upload, such as spigotmc.ru

---

## Documentation Rules

`docs/content/` has four sections, each a root folder: `admins/` (Server admins), `configuration/` (Configuration reference), `developers/` and `contributing/`.

- Pages outside Developers and Contributing never name classes, methods, packages or modules; say what the admin or player notices.
- The configuration reference has one page per file, each field a `<Setting>`.
- A folder exists only for two or more pages; each page is `<name>/index.md` (or `.mdx`), and each folder with several pages has a `meta.json`.
- Describe the current product; update the matching page in the same work as a user-visible change.

---

## Working Style

- Match the conventions already established in the surrounding module.
- Prefer small, scoped changes over broad cleanup unless cleanup is part of the task.
- Settle structure and naming with the owner as tree variants, one topic at a time, before building.
