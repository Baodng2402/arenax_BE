# API Gateway

Responsibilities:

- route external HTTP traffic to service modules
- propagate or generate `X-Request-Id`
- expose actuator health

Current default downstream URLs:

- identity: `http://localhost:8081`
- access: `http://localhost:8082`
- tenant: `http://localhost:8083`
- subscription: `http://localhost:8084`
- competition: `http://localhost:8085`
- ranking: `http://localhost:8086`
