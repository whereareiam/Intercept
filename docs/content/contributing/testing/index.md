---
title: Live tests
description: Run the journeys on real Paper, Velocity and NeoForge servers.
---

`intercept-testing` starts the packaged plugin and mod on real servers through Anvil and drives simulated players
through them. Each platform has a network of two instances on a PostgreSQL database and a Redis server, both in
Docker containers, so the journeys need Docker.

```bash
./gradlew :intercept-testing:anvilPaperTest
./gradlew :intercept-testing:anvilVelocityTest
./gradlew :intercept-testing:anvilNeoforgeTest
```

Every platform's journeys show:

- a key rendered in the language the player chose, and a marked system message translated on its way;
- a language chosen on one instance applying on the other at once;
- a message published on one instance shown on the other.

On Paper, a probe plugin with its own translator shows that the network's text wins for a key both have, and that
the probe's other keys keep working.

A journey class carries its platform's tag (`paper`, `velocity`, `neoforge`). A failed journey keeps its network
under `intercept-testing/build/anvil/`; each process's `anvil-console.log` tells what it did.
