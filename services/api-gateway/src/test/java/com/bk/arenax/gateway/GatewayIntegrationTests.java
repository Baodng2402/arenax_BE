package com.bk.arenax.gateway;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.proc.SecurityContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayIntegrationTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static HttpServer jwksServer;
    private static HttpServer downstreamServer;
    private static RSAKey rsaKey;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void setUpServers() throws Exception {
        rsaKey = new RSAKey.Builder(readPublicKey())
                .privateKey(readPrivateKey())
                .keyID("gateway-test-key")
                .build();

        jwksServer = HttpServer.create(new InetSocketAddress(0), 0);
        jwksServer.createContext("/.well-known/jwks.json", exchange -> writeJson(exchange, new JWKSet(rsaKey.toPublicJWK()).toJSONObject()));
        jwksServer.start();

        downstreamServer = HttpServer.create(new InetSocketAddress(0), 0);
        downstreamServer.createContext("/", new EchoHeadersHandler());
        downstreamServer.start();
    }

    @AfterAll
    static void tearDownServers() {
        if (jwksServer != null) {
            jwksServer.stop(0);
        }
        if (downstreamServer != null) {
            downstreamServer.stop(0);
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("arenax.gateway.routes.identity-service", () -> "http://127.0.0.1:" + downstreamServer.getAddress().getPort());
        registry.add("arenax.security.jwt.issuer", () -> "arenax-identity");
        registry.add("arenax.security.jwt.audience", () -> "arenax-api");
        registry.add("arenax.security.jwt.jwk-set-uri", () -> "http://127.0.0.1:" + jwksServer.getAddress().getPort() + "/.well-known/jwks.json");
    }

    @Test
    void publicAuthRouteAllowsAnonymousTrafficAndStripsSpoofedIdentityHeaders() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Arenax-User-Id", "spoofed-user")
                        .header("X-Request-Id", "req-auth-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "req-auth-1"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"))
                .andExpect(jsonPath("$.requestId").value("req-auth-1"))
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.authorization").doesNotExist());
    }

    @Test
    void protectedRouteRejectsMissingBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRouteInjectsTrustedHeadersAndRemovesAuthorizationBeforeProxying() throws Exception {
        String userId = UUID.randomUUID().toString();
        String sessionId = UUID.randomUUID().toString();
        String token = issueToken(userId, sessionId, "account-1", List.of("PLAYER", "OWNER"), List.of("MATCH:JOIN", "VENUE:BOOK"));

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Request-Id", "req-users-1")
                        .header("X-Arenax-User-Id", "spoofed-user")
                        .header("X-Arenax-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "req-users-1"))
                .andExpect(jsonPath("$.path").value("/api/v1/users/me"))
                .andExpect(jsonPath("$.requestId").value("req-users-1"))
                .andExpect(jsonPath("$.authorization").doesNotExist())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.accountId").value("account-1"))
                .andExpect(jsonPath("$.roles").value("PLAYER,OWNER"))
                .andExpect(jsonPath("$.permissions").value("MATCH:JOIN,VENUE:BOOK"));
    }

    private static String issueToken(
            String userId,
            String sessionId,
            String accountId,
            List<String> roles,
            List<String> permissions) {
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new com.nimbusds.jose.jwk.source.ImmutableJWKSet<>(new JWKSet(rsaKey)));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("arenax-identity")
                .audience(List.of("arenax-api"))
                .subject(userId)
                .issuedAt(now)
                .notBefore(now)
                .expiresAt(now.plusSeconds(600))
                .claim("sid", sessionId)
                .claim("token_version", 0)
                .claim("account_id", accountId)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .id(UUID.randomUUID().toString())
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                        JwsHeader.with(SignatureAlgorithm.RS256).keyId(rsaKey.getKeyID()).build(),
                        claims))
                .getTokenValue();
    }

    private static RSAPrivateKey readPrivateKey() throws Exception {
        String pem = new String(new ClassPathResource("identity-test-private.pem").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String sanitized = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] bytes = Base64.getDecoder().decode(sanitized);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    private static RSAPublicKey readPublicKey() throws Exception {
        String pem = new String(new ClassPathResource("identity-test-public.pem").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String sanitized = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] bytes = Base64.getDecoder().decode(sanitized);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
    }

    private static void writeJson(HttpExchange exchange, Object body) throws IOException {
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private static final class EchoHeadersHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("path", exchange.getRequestURI().getPath());
            putIfPresent(body, exchange, "Authorization");
            putIfPresent(body, exchange, "X-Request-Id");
            putIfPresent(body, exchange, "X-Arenax-User-Id");
            putIfPresent(body, exchange, "X-Arenax-Session-Id");
            putIfPresent(body, exchange, "X-Arenax-Account-Id");
            putIfPresent(body, exchange, "X-Arenax-Roles");
            putIfPresent(body, exchange, "X-Arenax-Permissions");
            writeJson(exchange, body);
        }

        private void putIfPresent(Map<String, Object> body, HttpExchange exchange, String headerName) {
            String value = exchange.getRequestHeaders().getFirst(headerName);
            if (value != null) {
                body.put(switch (headerName) {
                    case "Authorization" -> "authorization";
                    case "X-Request-Id" -> "requestId";
                    case "X-Arenax-User-Id" -> "userId";
                    case "X-Arenax-Session-Id" -> "sessionId";
                    case "X-Arenax-Account-Id" -> "accountId";
                    case "X-Arenax-Roles" -> "roles";
                    case "X-Arenax-Permissions" -> "permissions";
                    default -> headerName;
                }, value);
            }
        }
    }
}
