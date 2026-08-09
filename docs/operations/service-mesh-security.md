# ArenaX Service Mesh Security Baseline

Doc này mô tả baseline production cho mô hình `Gateway-only public access` đã implement trong code.

## Public Entry Rule

- Chỉ `api-gateway` được publish ra ngoài cluster/VPC.
- Public HTTPS có thể terminate ở Kubernetes Ingress, cloud load balancer, hoặc edge proxy tương đương.
- `identity-service`, `tenant-service`, `subscription-service`, `competition-service`, và `ranking-service` không được có public ingress riêng.

## Kubernetes Network Shape

- Tất cả business service và `identity-service` phải là `ClusterIP`.
- Bật default-deny `NetworkPolicy` cho namespace ứng dụng.
- Chỉ cho phép ingress HTTP vào API path nội bộ từ workload `api-gateway`.
- Chỉ cho phép service-to-service `/internal/v1/**` giữa workload đã được phê duyệt.
- `discovery-server` chỉ mở đúng port/service discovery cần thiết cho nội bộ cluster.

## mTLS And Authorization Policy

- Service mesh phải bật strict mTLS cho east-west traffic.
- Policy L7 phải tách rõ:
  - `api-gateway -> */api/**`
  - `service-a -> service-b/internal/v1/**`
- Internal caller authorization phải dựa trên workload identity của mesh, không dựa vào header do caller tự gửi.

## Gateway Trust Boundary

- Gateway là nơi duy nhất verify end-user JWT.
- Sau khi verify, gateway strip `Authorization` và mọi header `X-Arenax-*` từ request protected route trước khi inject trusted headers mới.
- Business service chỉ được tin `X-Arenax-*` cho traffic `/api/**` nếu traffic đó chỉ có thể đến từ `api-gateway` theo infra policy.
- Internal `/internal/v1/**` endpoint không được suy diễn end-user identity từ `X-Arenax-*`.

## Local Development Rule

- `compose.yaml` hiện chỉ expose PostgreSQL, Redis và `discovery-server`, chưa publish service runtime nào.
- Khi thêm local app containers sau này, chỉ expose `api-gateway` mặc định.
- Business service port chỉ mở tạm thời khi debug cục bộ và không được commit thành default shared workflow.

## Deployment Checklist

- Public DNS chỉ trỏ vào `api-gateway` edge.
- Không có `LoadBalancer`/`NodePort` riêng cho business service.
- Namespace có default-deny `NetworkPolicy`.
- Mesh strict mTLS đang enforced.
- AuthorizationPolicy ghi rõ caller workload, method, và path nội bộ.
- Request tracing giữ `X-Request-Id` xuyên gateway và internal call.
