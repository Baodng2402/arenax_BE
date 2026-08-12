# ArenaX Event Conventions

## Envelope

All integration events use this envelope:

```json
{
  "eventId": "uuid",
  "eventType": "identity.user.registered.v2",
  "eventVersion": 2,
  "occurredAt": "2026-07-13T10:00:00Z",
  "correlationId": "uuid",
  "producer": "identity-service",
  "payload": {}
}
```

## Rules

- `eventId` is globally unique and used for idempotency.
- `correlationId` ties one business workflow across services.
- `eventType` is immutable once published.
- Breaking payload changes require a new event version.
- Producers write to an outbox in the same local transaction as business state.
- Consumers store handled `eventId` values to avoid duplicate processing.
