# VPS Test Backend

This note documents the temporary FreeLink backend deployment used for Android MVP smoke testing.

## Public Endpoints

- API: `http://130.49.128.205:8080`
- WS gateway: `ws://130.49.128.205:8081`
- Test APK download: `http://130.49.128.205:8090/freelink-vps-debug.apk`

## Health Checks

```bash
curl http://130.49.128.205:8080/health
curl http://130.49.128.205:8080/api/v1/ping
curl http://130.49.128.205:8081/health
```

## Android Debug Build

```bash
./gradlew :android:app:assembleDebug \
  -Pfreelink.apiBaseUrl=http://130.49.128.205:8080 \
  -Pfreelink.chatListWsUrl=ws://130.49.128.205:8081/ws/chats \
  -Pfreelink.directChatWsUrl=ws://130.49.128.205:8081/ws/messages
```

The generated APK is copied for convenience to:

```text
build/deploy/freelink-vps-debug.apk
```

## Services

The VPS runs these systemd services:

```bash
systemctl status freelink-api
systemctl status freelink-ws-gateway
systemctl status freelink-worker
systemctl status freelink-downloads
```

## Important Limits

- This deployment is for MVP smoke testing only.
- API state is currently in-memory, so registered users and sessions are lost after API restart.
- The debug APK allows cleartext HTTP only for test builds. Release builds keep cleartext disabled.
- Do not publish this setup as production until TLS, persistent storage, migrations, and production secrets are wired.
