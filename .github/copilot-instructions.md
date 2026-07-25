# Zephyr Project Guidelines

## Code Style

- Write new code in Kotlin.
- Prefer stateless, functional UI: small composables, pure helper functions, hoisted state, and minimal mutable state.
- Keep Android framework classes thin; put business logic and data mapping in testable Kotlin classes and functions.
- Keep Wear UI, tile, complication, and phone app code paths separate unless a shared abstraction is clearly justified.

## Architecture

- Zephyr is a windsurfing app with a Wear OS app, a tile, a complication, and a companion phone app.
- The project targets API 36 as the minimum SDK. Do not add compatibility workarounds for older Android versions.
- Prefer dependency injection or constructor injection for anything that needs to be mocked.
- Health data comes from the Health Services API only. Prefer tracking location etc using the Health Services API.

### App Structure

Use the following app structure when building an exercise app with Health Services:

- Keep your screens and navigation within a main activity.
- Manage the workout state, sensor data, ongoing activity, and data with a foreground service.
- Store data with Room.

### Domain Driven Design

1. Domain layer

- Pure Kotlin models: SessionState, LiveMetrics, WindObservation, WindForecast, SurfaceSnapshot.
- Repository interfaces only.
- Use cases orchestrate multiple repositories and map to UI models.

2. Data layer

- Data sources wrap Health Services, network wind API, Room/DataStore.
- Repository implementations combine remote + local + stream state.
- Mappers convert DTO/entity/platform objects to domain objects.

3. Surface layer

- App screen ViewModels consume use cases as Flow/StateFlow.
- Tile and complication never call Health Services directly.
- Tile and complication read from snapshot repository (last known, fast, resilient).

4. Runtime layer

- Foreground workout service owns active exercise lifecycle and sensors.
- Service writes live metrics + checkpoints into repositories.
- UI/tile/complication subscribe to repository outputs, not service internals.

### Wear-Specific Flow

1. Workout starts in app UI.
2. Use case tells ExerciseSessionRepository to start.
3. Foreground service collects metrics and writes repository state.
4. Snapshot updater use case produces compact SurfaceSnapshot records.
5. App UI observes detailed state; tile/complication observe compact snapshot state.
6. Wind sync worker periodically refreshes WindRepository cache.

### Data Ownership and Boundaries

- Health Services API access only inside health data source classes.
- Network client only inside wind remote data source.
- Room DAOs only inside local data sources.
- ViewModels, tile service, complication service only talk to use cases
- Repositories must not be accessed by ViewModels directly, only through use cases

### Error and Offline Strategy

- Repository return type should encode Loading, Data, and Error with stale metadata.
- Wind reads should use local-first + background refresh.
- Tile/complication must always have fallback snapshot text if network/health unavailable.
- Keep timeouts strict for watch battery and responsiveness.

### Dependency Injection

- Constructor injection everywhere.
- Start simple with manual DI module object, then move to Hilt if desired.
- Inject repository interfaces into use cases and ViewModels/services.
- Provide fake repositories for tests and previews.

### Testing Strategy

- Unit tests at use case and repository implementation levels first.
- Fake health data source and fake wind API for deterministic tests.
- Contract tests for repository freshness rules and snapshot generation.
- Tile/complication tests verify fallback and stale rendering behavior.

## Build and Test

- Use the Gradle wrapper from the repository root.
- Run unit tests with `./gradlew test` or the narrowest affected module task.
- Add or update unit tests for every behavior change. If code is hard to unit test, add seams instead of relying on instrumentation tests alone.

## Conventions

- Treat tiles and complications as separate delivery surfaces with their own logic and previews.
- Focus UI and business logic on surfacing windsurfing metrics such as speed, wind speed, heading, and heart rate.
- When adding health-driven features, update the abstraction, fake, and tests together.
- Prefer simple, composable APIs over deep inheritance.

## Incremental Rollout Plan

1. Foundation

- Add domain models, repository interfaces, result/error abstractions.
- Add coroutine, Room, DataStore, and Health Services dependencies.

2. Health vertical slice

- Implement ExerciseSessionRepository + LiveMetricsRepository with fake first, then real data source.
- Wire app screen to show live session and metrics.

3. Wind vertical slice

- Implement WindRepository with cache-first strategy and refresh policy.
- Expose wind summary in app screen.

4. Surface snapshots

- Build SurfaceSnapshotRepository and updater use case.
- Switch tile and complication to snapshot reads.

5. Hardening

- Add tests for repository contracts and stale/offline behavior.
- Add battery-aware update policies and backoff rules.

## Presentation

- Build the presentation layer using the Material 3 Design system
- Adhere to Android and Material 3 Design Rules
