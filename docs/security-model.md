# Security Model

This document captures authentication, key management, and threat controls.

## Authentication

- Login/password only (no phone number).
- Password hashing target: Argon2id.
- Anti-enumeration and rate limiting required.
- Refresh token rotation and device revocation required.

## Message crypto

- Signal-style foundation (device identity keys + pre-keys + ratcheting).
- Versioned encrypted envelope.
- Server relays ciphertext and stores minimal metadata.

## Android hardening

- Android Keystore for local key storage.
- BiometricPrompt for lock-sensitive operations.
- Hidden mode covers notification redaction and recent-app preview protection.
- Android app backup and device-transfer extraction are explicitly disabled for protected local data.
- Release cleartext traffic is disabled; debug keeps local HTTP access for emulator development only.
- Backend logging must use the observability safe logger/redactor for tokens, passwords and secrets.
- No secret logging, analytics leakage, or clipboard leakage.
