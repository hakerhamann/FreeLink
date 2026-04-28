# Architecture

This document describes the target architecture of FreeLink across Android and Backend modules.

## Monorepo layout

- `android/`: multi-module Android app and feature modules.
- `backend/`: Ktor modular monolith (`apps/*` + `libs/*`).
- `build-logic/`: shared convention plugins.
- `docs/`: architecture, security, testing, and release docs.

## Android layering

- `app` as composition root.
- `core/*` for reusable abstractions and platform infrastructure.
- `feature/*` for user flows and screens.
- UDF + ViewModel state holders by default.

## Backend layering

- `apps/api`: REST boundary.
- `apps/ws-gateway`: websocket boundary.
- `apps/worker`: background processing.
- `libs/*`: domain and infra capabilities.

## Security baseline

- Password auth is isolated from message crypto.
- Encrypted envelope is versioned and transport-safe.
- No secret logging policy.
