# Sprint 7 Stabilization Checklist

Use this checklist for each release-candidate PR before tagging the MVP.

## Build Gates

- `./gradlew :backend:apps:api:compileKotlin :backend:apps:ws-gateway:compileKotlin :backend:apps:worker:compileKotlin`
- `./gradlew :backend:libs:auth:test :backend:libs:chats:test :backend:libs:messaging:test :backend:libs:media:test :backend:libs:privacy:test :backend:libs:observability:test`
- `./gradlew :android:app:compileDebugKotlin`
- `./gradlew :android:feature:chat:testDebugUnitTest :android:feature:settings:testDebugUnitTest`
- `./gradlew :android:benchmark:test :android:baselineprofile:test`
- `./gradlew :android:app:processDebugManifest :android:app:processReleaseManifest`

These gates are mirrored in GitHub Actions CI for release-candidate PRs.

## Manual Scenarios

- Fresh install opens auth and reaches chat list after login.
- Existing session survives app restart and protected API calls refresh after token expiry.
- Hidden mode applies `FLAG_SECURE`; disabling it restores normal preview behavior.
- Biometric lock blocks app content until successful unlock.
- Link previews appear only when enabled.
- Disappearing messages show expiry labels and no longer appear after backend expiry.
- Unknown chat IDs are blocked by trusted messaging policy.
- Archive and restore keep main chat list and archive screen consistent.
- Device revoke removes only the selected session.
- Media dedup returns existing blob metadata without reuploading.
- Performance scenario catalog covers startup, chat list, open chat, media and voice.

## Release Review Questions

- Did this PR add or change an externally observable contract?
- Did docs/API contracts/security notes change with the behavior?
- Could logs, crash reports, screenshots, clipboard, backups, or manifests leak protected data?
- Is there a rollback path that does not require destructive data changes?
- Is the change small enough to revert independently?
