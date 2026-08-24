> **Contracts entrypoint** - đọc file này khi bạn cần biết spec nào là canonical source of truth cho HTTP, event, hoặc security boundary.

# Contracts Guide

`docs/contracts/` giữ shared agreement giữa services và edge của hệ thống. Đây là spec/reference layer, không phải runtime module và không chứa Java DTO jar để import vào code.

## Start Here

1. Đọc `README.md` rồi `docs/overview.md` để nắm domain và integration model.
2. Đọc `docs/architecture/README.md`, rồi vào `conventions.md` nếu bạn sắp thay đổi flow cross-service.
3. Chọn loại contract phù hợp bên dưới.

## File Map

- `asyncapi/arenax-events.yaml`: canonical event contract cho integration events hiện đang được implement trong repo.
- `asyncapi/examples/`: example payloads để review shape thực tế của event.
- `openapi/*.yaml`: canonical public HTTP contract cho service đã có spec.
- `internal-api/README.md`: rulebook cho internal synchronous HTTP giữa services.
- `security/*.md`: trust-boundary và JWT profile specs.

## Reading Paths

- Nếu bạn sắp thêm event mới: đọc `asyncapi/arenax-events.yaml` rồi `../how-to/add-a-new-event-flow.md`.
- Nếu bạn sắp thêm public HTTP behavior cho service đã có OpenAPI: update file phù hợp trong `openapi/`, rồi sync lại service doc liên quan.
- Nếu bạn sắp thêm internal HTTP: đọc `internal-api/README.md` rồi `../how-to/add-an-internal-http-call.md`.
- Nếu bạn sắp thay trust boundary hoặc JWT shape: đọc cả hai file trong `security/` trước khi đổi code.

## Boundaries For This Folder

- Chỉ giữ spec/agreement ở đây.
- Business rule, package layout, và coding rule tổng quát vẫn thuộc `docs/architecture/`.
- Runtime implementation note theo từng module vẫn thuộc `docs/services/`.
