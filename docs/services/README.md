# Service Notes Guide

`docs/services/` là chỗ deep-dive theo từng service sau khi bạn đã biết mình sẽ sửa module nào.

## Start Here

1. Đọc `README.md` rồi `docs/overview.md` để nắm repo map.
2. Đọc `docs/architecture/README.md` để biết boundary và rule chung.
3. Chọn file service tương ứng bên dưới.

## Service Files

- `identity.md`: users, authentication, sessions, RBAC, JWT issuance.
- `tenant.md`: accounts và memberships.
- `subscription.md`: subscription lifecycle và onboarding side effects.
- `competition.md`: sports, matches, participants, match completion.
- `ranking.md`: ranking projection, ELO updates, history.
- `api-gateway.md`: ingress routing và trust boundary.

## Historical Note

- `access.md` chỉ là tombstone lịch sử. `access-service` đã merge vào `identity-service`.
