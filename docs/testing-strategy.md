# Testing Strategy

This document captures test pyramid and performance validation for Android and Backend.

## Android

- Unit tests for reducers/use-cases/repositories.
- UI tests for auth, chat list, direct chat, groups, privacy toggles.
- Screenshot tests for visual parity against design renders.
- Macrobenchmark for startup, chat list, open chat, media and voice scenarios.

## Backend

- Unit tests for domain and service logic.
- Repository tests for PostgreSQL/Redis paths.
- WebSocket session tests for typing/delivery/read/reconnect.
- E2E for auth, messaging, media dedup, privacy, archive.

## CI gating

- Backend Kotlin compile checks.
- Android compile checks.
- Expand to lint/test suites as features land.
