# Global Agent Rules (FreeLink)

## Engineering principles
- Keep modules small and cohesive.
- Prefer composition over inheritance.
- Keep files below 300 lines where practical.
- Use explicit naming and short comments in English.

## Mandatory separation
- API DTO
- DB entity
- Domain model
- UI model
- Mapper layer

## Android architecture
- Jetpack Compose UI
- ViewModel state holders
- Unidirectional data flow
- Hilt for DI

## Backend architecture
- Kotlin + Ktor modular monolith
- PostgreSQL for source-of-truth persistence
- Redis for ephemeral and queue workloads
- S3-compatible blob storage for media

## Security baseline
- No secret logging
- Argon2id for password hashing
- TLS-only transport
- Strong defaults for privacy toggles

## Delivery workflow
- Conventional commits
- Small PR-sized changes
- CI green before merge
- Docs updated with each meaningful architecture change
