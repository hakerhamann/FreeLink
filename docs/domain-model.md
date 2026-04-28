# Domain Model (v1 skeleton)

## Core entities

- User
- DeviceSession
- Chat (direct/group)
- Message and MessageEnvelope
- MediaAttachment
- PrivacySettings

## Mapping policy

- API DTO: transport schema only.
- DB entity: persistence schema only.
- Domain model: business rules and use-cases.
- UI model: presentation-only fields and formatting.

Each boundary crossing must be explicit through mapper classes/functions.
