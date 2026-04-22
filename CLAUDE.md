# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build (produces a shaded/fat JAR via shadowJar)
./gradlew build

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "fr.milekat.utils.ToolsTest"

# Publish to GitHub Packages (requires gpr.user and gpr.key in gradle.properties or env vars USERNAME/TOKEN)
./gradlew publish

# Publish to local staging directory (build/staging-deploy)
./gradlew publishMavenPublicationToStagingDeployRepository
```

Tests must match the pattern `*Test` (configured in `build.gradle`). Test dependencies extend `compileOnly`, so test code has access to all optional dependencies (Spigot, MySQL, Elasticsearch, etc.).

## Architecture Overview

MilekatUtils is a Java utility library (`fr.milekat.utils`) for Minecraft (Spigot/Bukkit) plugins. It is published as a Maven artifact (`fr.milekat:milekat-utils`) with shaded dependencies (json, snakeyaml, HikariCP, slf4j are relocated under `fr.milekat.utils.lib.*`).

### Core Utilities (`fr.milekat.utils`)

- **`Configs`** — YAML config reader (wraps SnakeYAML). Reads dot-notation paths (e.g. `storage.type`). Also handles Minecraft color code translation via `getMessage()`.
- **`MileLogger`** — Logging wrapper used throughout the library.
- **`McTools` / `McNames` / `DateMileKat` / `Tools`** — Minecraft and general utility helpers.

### Storage Subsystem (`fr.milekat.utils.storage`)

The storage system is loaded via `StorageLoader(Configs, MileLogger)`, which reads `storage.type` from config and returns a `StorageConnection`. Available vendors are defined in `StorageVendor` enum:

- **SQL** (`mysql` / `mariadb` / `postgres`): `SQLConnection` via HikariCP. The driver JAR must be on the classpath at runtime — they are `compileOnly` in this library.
- **Elasticsearch** (`es` / `elastic` / `elasticsearch`): `ESConnection` wraps the Elastic Java client (9.x). Supports API key or username/password auth, optional SSL fingerprint.

Adapter selection is dynamic: `StorageAdapterLoader` iterates all `StorageVendor` values and attempts `Class.forName` on the driver class; only adapters whose driver is present on the classpath are instantiated. This means SQL and Elasticsearch adapters coexist without compile errors even if only one driver is present.

The `Index` class (Elasticsearch) creates indices with strict dynamic mapping on startup if they don't already exist. Minecraft-specific Jackson serializers/deserializers live under `storage.adapter.elasticsearch.mappers`.

### Messaging Subsystem (`fr.milekat.utils.messaging`)

Loaded via `MessagingLoader(Configs, MileLogger)`, which reads `messaging.type`. Follows the same dynamic adapter pattern as storage (`MessagingAdapterLoader` / `MessagingVendor`):

- **RabbitMQ**: `RabbitMQConnection` (driver: `com.rabbitmq.client.Connection`)
- **Redis**: `RedisConnection` (driver: `redis.clients.jedis.Jedis` — note: Jedis is not declared as a dependency in `build.gradle`, consumer must provide it)

`MessagingConnection` interface supports two consumer modes:

- **Topic mode** (`registerMessageProcessor`) — each processor gets its own exclusive, auto-delete queue bound to the exchange via a routing key pattern. Every subscriber matching the pattern receives every message (pub/sub fan-out). Always active.
- **Task queue mode** (`registerTaskProcessor`) — all workers share a single durable, non-exclusive queue. Each message is delivered to exactly one worker (competing consumers). Workers start **inactive** and must call `setProcessorActive(name, true)` when ready to accept tasks, and `setProcessorActive(name, false)` when busy. Internally toggles `basicCancel` / `basicConsume` on a persistent dedicated channel — no connect/disconnect needed.

`MessagingConnection` key methods:
- `sendMessage(routingKey, message)` / `sendMessage(routingKey, callbackKey, message)`
- `registerMessageProcessor(name, routingKey, handler)` — topic mode
- `registerTaskProcessor(name, queueName, handler)` — task queue mode, starts inactive
- `setProcessorActive(name, boolean)` — activate/deactivate a task processor
- `unregisterMessageProcessor(name)` — stops and removes any processor type

**`RabbitMQConnection` internals** — four tracking maps:
- `registeredProcessors` (`ProcessorConfig` record) — config for auto-recovery after reconnect; `isTaskQueue` + `active` flags drive `reRegisterAllConsumers()`
- `processorConsumerTags` — processorName → AMQP consumerTag, used for targeted `basicCancel`
- `activeConsumers` — consumerTag → Channel
- `taskChannels` — processorName → dedicated Channel for task processors (persists across pause/resume)

`buildDeliverCallback(channel, routingKey, handler)` is a shared helper used by both `createConsumer` (topic) and `createTaskConsumer` (task) to avoid logic duplication.

### Adding a New Storage or Messaging Vendor

1. Add a new entry to `StorageVendor` or `MessagingVendor` enum with the adapter name, connection class name, and driver class for detection.
2. Create the connection class at the expected package path (`fr.milekat.utils.storage.adapter.<adapter>/connection/<Class>` or `fr.milekat.utils.messaging.adapter.<adapter>/<Class>`).
3. Implement `StorageConnection` or `MessagingConnection`. The constructor must accept `(Configs, MileLogger)`.
4. Add the driver as `compileOnly` in `build.gradle`.