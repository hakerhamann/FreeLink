# MVP Release Runbook

This runbook defines the manual and automated gates for a safe FreeLink MVP release.

## Release Inputs

- Target tag: `v1.0.0-mvp`.
- Target branch: protected `main`.
- Required environment: Local and Staging.
- Required artifacts: Android debug/staging APK from CI artifact `freelink-debug-apk`, backend API image, WS gateway image, worker image.

## Go/No-Go Gates

- CI is green for backend compile, Android compile, and focused test suites.
- No open P0/P1 bugs in auth, messaging, privacy, media, archive, or device sessions.
- Security review confirms no secret logging, no Android backup extraction, and release cleartext disabled.
- API contracts and user-facing behavior notes are updated.
- Rollback owner and incident channel are known before tagging.

## Android Regression

- Auth: register, login, refresh retry after `401`, logout.
- Devices: list sessions, revoke non-current device, logout current session.
- Chat list: sync from API, search, unread filter, archive action.
- Direct chat: load history, send text, reply, reaction, receipt label, typing indicator, realtime WS event handling.
- Privacy: hidden mode recent-app protection, biometric lock, disappearing message label, link preview toggle.
- Archive: list archived chats, restore archived chat.
- Media: photo, video, file, and voice attachment presets; open attachment URL.
- Groups and profile: group list/details/open shared chat, profile shared sections.

## Backend Regression

- Auth: register/login/refresh rotation/logout/device revoke.
- Messaging: list/send, attachment-only send, reply, reactions, trusted-chat policy, disappearing expiration filter.
- Media: init dedup path, complete upload, already-exists path.
- Groups: list/create/detail with members and roles.
- Privacy: get/update settings, default hardening values.
- Archive: archive/list/restore lifecycle.
- WS: chat updates, message created, typing, delivered/read, reconnect smoke.

## Staging Smoke Order

1. Start Postgres, Redis, MinIO, API, WS gateway, and worker.
2. Create two users and register at least two devices for one user.
3. Verify API `/health` and `/api/v1/ping`.
4. Run Android auth, chat list, direct chat, media, privacy, archive and logout smoke.
5. Exercise WS by opening a chat and sending typing/message events.
6. Confirm logs contain no access tokens, refresh tokens, passwords, Authorization headers, or blob secrets.

## APK Download

- Local debug APK path after `./gradlew :android:app:assembleDebug`: `android/app/build/outputs/apk/debug/app-debug.apk`.
- GitHub Actions artifact name: `freelink-debug-apk`.
- Do not promote the APK if Sprint 7 regression gates or smoke checks fail.

## Rollback

- Android: keep previous APK artifact available and do not promote the new APK if smoke fails.
- Backend: redeploy previous API, WS gateway, and worker images.
- Data: do not run destructive migrations for MVP without a verified backup and restore rehearsal.
- Communications: publish a short incident note with impact, rollback status, and next action.

## Release Notes Template

```markdown
# FreeLink v1.0.0-mvp

## Highlights
- Secure login/device sessions.
- Chat list, direct chat, groups, media and archive.
- Privacy controls for hidden mode, biometric lock, disappearing messages, link previews and trusted chats.

## Known Gaps
- Calls and OTA APK update flow are planned for wave 2.
- Production database/object-storage hardening follows after staging validation.

## Validation
- CI checks:
- Android smoke:
- Backend smoke:
- WS smoke:
```
