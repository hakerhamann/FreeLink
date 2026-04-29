# API and WS Contracts (v1 Skeleton)

## REST endpoints

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /devices`
- `DELETE /devices/{deviceId}`
- `GET /chats`
- `GET /people`
- `GET /messages?chatId=`
- `POST /messages`
- `GET /groups`
- `GET /groups/{groupId}`
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
- `GET /chats` uses `Authorization: Bearer <accessToken>` and supports optional `q` and `unreadOnly` query params.
- `GET /messages?chatId=` and `POST /messages` use `Authorization: Bearer <accessToken>`.
- `POST /messages/{messageId}/reactions` uses `Authorization: Bearer <accessToken>`.
- `POST /messages` accepts plain `body` and optional encrypted envelope payload for E2EE transport shape.
- `POST /messages` optionally accepts `replyToMessageId`; `GET /messages` returns `replyToMessageId` and `reactions`.
- `POST /messages` now also accepts optional `attachment` metadata; attachment-only messages are valid when `body` is blank.
- Privacy defaults are server-defined (`hiddenMode=false`, `biometricLock=false`, `linkPreview=true`, `whoCanMessageMe=trusted_contacts`).
- On Android, protected device requests now retry once after `401` via `/auth/refresh`.
- On Android, privacy and device protected requests retry once after `401` via `/auth/refresh`.
- On Android, chat list now syncs from API to Room (`core:database`) and applies search/unread filtering from local cache.
- On Android, people list now syncs from API to Room and supports local search by display name/login.
- On Android chat tab, a single search query now matches both local chats and local people cache.
- On Android direct chat, message history and send action are now backed by `GET/POST /messages` with token refresh fallback.
- On Android direct chat, outgoing `POST /messages` now attaches a client-generated envelope (`version=1`, metadata + transport ciphertext shape).
- On Android direct chat, message reply and reaction actions are now wired to messages API.
- `GET /groups` and `POST /groups` are now available in backend API with bearer auth.
- `GET /groups/{groupId}` now returns members and role-aware group details for the selected group.
- On Android `Spaces` tab, group list refresh/search and group creation are now wired to `/groups`.
- On Android `Spaces` tab, selected groups now show members/roles and can open the shared group chat screen.
- `POST /media/init` now returns upload session metadata (`uploadId`, `blobKey`, `uploadUrl`, `alreadyExists`) for dedup-aware media flow.
- `POST /media/complete` now finalizes a session into stored media metadata with a blob download URL.
- On Android, `Media Gallery` now provides a manual Sprint 5 smoke-flow for `init upload` and `complete upload`, reachable from profile shared photos.
- On Android direct chat, `Attach sample` now runs media `init/complete`, keeps pending attachment state in composer, and renders attachment cards inside message bubbles.
- On Android direct chat, the composer now exposes a small attachment tray for `Photo / File / Voice` presets wired to the same media upload flow.
- On Android direct chat, attachment cards now expose an `Open` action backed by the media `downloadUrl`.
- On Android direct chat, outgoing messages now show local receipt progression (`sent -> delivered -> read`) and short peer typing indicator UX.
- WS gateway `/ws/chats` now requires `Authorization: Bearer <accessToken>` and emits `chat.updated` for chat list resync.
- WS gateway `/ws/messages?chatId=` now requires `Authorization: Bearer <accessToken>` and emits `typing.*`, `message.created`, `receipt.*` events.
- On Android direct chat, realtime `typing/message/receipt` updates are now consumed from WS events.
