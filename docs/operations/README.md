# Operations Guide

`docs/operations/` là chỗ cho production-oriented vận hành và infrastructure policy. Đây không phải nơi để đọc local development hay implementation flow.

## Start Here

1. Đọc `README.md` rồi `docs/overview.md` để nắm system shape.
2. Đọc `docs/architecture/README.md` nếu bạn cần hiểu trust boundary hay internal API rules trước.
3. Chọn doc vận hành phù hợp bên dưới.

## File Map

- `service-mesh-security.md`: baseline production cho gateway-only public access, mTLS, network policy, và trusted-header boundary.

## Boundaries For This Folder

- Local dev commands và laptop workflow nằm ở `docs/development/`.
- API contract/spec nằm ở `docs/contracts/`.
- Repo-wide coding rules nằm ở `docs/architecture/conventions.md`.
