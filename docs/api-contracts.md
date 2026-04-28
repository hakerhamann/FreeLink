# API and WS Contracts (v1 Skeleton)

## REST endpoints

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /devices`
- `DELETE /devices/{deviceId}`
- `GET /chats`
- `GET /messages?chatId=`
- `POST /messages`
- `POST /groups`
- `POST /media/init`
- `POST /media/complete`
- `GET /privacy/settings`
- `PUT /privacy/settings`
- `POST /archive/{chatId}`

## WebSocket events

- `message.created`
- `message.updated`
- `typing.started`
- `typing.stopped`
- `receipt.delivered`
- `receipt.read`
- `reaction.set`
- `chat.updated`

## Envelope schema

```json
{
  "version": 1,
  "conversationId": "chat-uuid",
  "senderDeviceId": "device-uuid",
  "ciphertext": "base64",
  "nonce": "base64",
  "sentAt": "2026-04-28T12:34:56Z"
}
```

## Sprint 1 notes

- Current scaffold uses in-memory auth/session storage for local development.
- `GET /devices` and `DELETE /devices/{deviceId}` use `Authorization: Bearer <accessToken>`.
- `GET /privacy/settings` and `PUT /privacy/settings` use `Authorization: Bearer <accessToken>`.
- Privacy defaults are server-defined (`hiddenMode=false`, `biometricLock=false`, `linkPreview=true`, `whoCanMessageMe=trusted_contacts`).
- On Android, protected device requests now retry once after `401` via `/auth/refresh`.
- On Android, privacy and device protected requests retry once after `401` via `/auth/refresh`.
