# Zephyr

![Mobile tests](https://github.com/tom-heidenreich/Zephyr/actions/workflows/mobile-tests.yml/badge.svg)
![Wear tests](https://github.com/tom-heidenreich/Zephyr/actions/workflows/wear-tests.yml/badge.svg)

> This project is an experiment to see how far you can get with only vibe coding

Zephyr is a sailing and windsurfing companion app built around a Wear OS watch experience. It helps a rider track an active session, derive sailing metrics from live data, and surface compact status views on the watch, tile, and complication.

The app is designed for fast, glanceable use on the water:

- The Wear OS app is the primary experience. It shows the active session, live metrics, wind context, and simple controls for starting, pausing, resuming, and ending a workout.
- The Tile provides a quick watch-face-adjacent summary for the current session and conditions.
- The Complication exposes a minimal status view for watch faces.
- The mobile app is a companion phone shell that shares the same app identity and launcher flow.

## Project Structure

The repository is split into two Android application modules:

- `wear/` - the main Wear OS app, tile, complication, and shared runtime/domain/data layers.
- `mobile/` - the companion phone app.

A simplified view of the Wear module looks like this:

```text
wear/
  src/main/java/com/tomheidenreich/zephyr/
    presentation/   Wear UI and the main activity
    tile/           Tile service
    complication/   Complication data source
    runtime/        App graph, use case graph, and application setup
    domain/         Core models, repositories, and use cases
    data/           Fake and Health Services-backed repository implementations
    core/           Shared result and support types
```

### Wear Surface Overview

- Main activity: the watch dashboard and session controls.
- Tile service: a compact summary for quick glances.
- Complication service: a tiny status display for watch faces.
- Runtime graph: wires repositories and use cases together.
- Domain layer: defines the session, wind, metrics, and snapshot models.
- Data layer: provides fake repositories plus Health Services-backed implementations.

### Build Targets

- Wear app: `./gradlew :wear:compileDebugKotlin`
- Mobile app: standard Android application module under `mobile/`

## Continuous Integration

- Mobile tests run via GitHub Actions on pushes and pull requests for the mobile module.
- Wear tests run via GitHub Actions on pushes and pull requests for the wear module.

## Domain Focus

Zephyr centers on windsurfing and sailing data rather than generic fitness tracking. The watch experience is built to answer the questions a rider cares about most: current session state, heart rate, wind conditions, and compact sailing metrics that fit on a small screen.
