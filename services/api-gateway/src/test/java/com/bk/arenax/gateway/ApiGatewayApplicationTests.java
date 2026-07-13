package com.bk.arenax.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ApiGatewayApplication.class)
@AutoConfigureMockMvc
class ApiGatewayApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GatewayRouteProperties gatewayRouteProperties;

    @Test
    void contextLoads() {
        assertThat(gatewayRouteProperties.identityService()).isEqualTo("http://localhost:8081");
        assertThat(gatewayRouteProperties.accessService()).isEqualTo("http://localhost:8082");
        assertThat(gatewayRouteProperties.tenantService()).isEqualTo("http://localhost:8083");
        assertThat(gatewayRouteProperties.subscriptionService()).isEqualTo("http://localhost:8084");
        assertThat(gatewayRouteProperties.competitionService()).isEqualTo("http://localhost:8085");
        assertThat(gatewayRouteProperties.rankingService()).isEqualTo("http://localhost:8086");
    }

    @Test
    void healthAddsGeneratedRequestIdWhenMissing() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void healthEchoesExistingRequestId() throws Exception {
        mockMvc.perform(get("/actuator/health").header("X-Request-Id", "req-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "req-123"));
    }
}
