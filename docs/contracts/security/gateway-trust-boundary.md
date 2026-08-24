> **Reference spec** - đọc file này khi bạn thay đổi public ingress, trusted headers, hoặc gateway-to-service trust assumptions.

# ArenaX Gateway Trust Boundary

## Public Entry Point

- Public HTTPS terminates only at API Gateway infrastructure.
- TLS may terminate at Kubernetes Ingress, cloud load balancer, API gateway appliance, or edge proxy.
- No business service may expose a public host port, public load balancer, or direct ingress route.

## Trusted User Context

API Gateway is the only component allowed to translate a verified end-user JWT into trusted user headers.

Gateway must:

- verify the bearer JWT against Identity JWKS
- remove inbound `Authorization` before forwarding unless the route explicitly needs it
- strip inbound `X-Arenax-User-Id`, `X-Arenax-Session-Id`, `X-Arenax-Account-Id`, `X-Arenax-Roles`, `X-Arenax-Permissions`
- inject those headers from verified claims only
- generate or forward a single `X-Request-Id`

Business services must:

- accept trusted user headers only on `/api/**`
- expose synchronous service-to-service APIs under `/internal/**`
- never treat user headers on `/internal/**` as trusted user identity
- authorize internal callers through workload identity and network policy, not by caller-provided headers

## Cluster Controls

- Kubernetes service type: `ClusterIP` for all business services and Identity.
- Default-deny `NetworkPolicy` between workloads.
- Allow `/api/**` traffic to business services only from Gateway workload.
- Allow `/internal/**` traffic only from explicitly approved caller workloads.
- Use service-mesh strict mTLS and L7 authorization policies for internal HTTP.

## Local Development

- `compose.yaml` should expose only Gateway publicly by default.
- Direct business service ports are temporary debugging exceptions and must be documented when used.

## Read Next

- `jwt-profile.md`
- `../../services/api-gateway.md`
- `../../operations/service-mesh-security.md`
