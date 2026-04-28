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
- No secret logging, analytics leakage, or clipboard leakage.
