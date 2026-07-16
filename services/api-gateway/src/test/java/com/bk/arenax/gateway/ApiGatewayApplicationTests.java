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
        assertThat(gatewayRouteProperties.identityService()).isEqualTo("lb://identity-service");
        assertThat(gatewayRouteProperties.accessService()).isEqualTo("lb://access-service");
        assertThat(gatewayRouteProperties.tenantService()).isEqualTo("lb://tenant-service");
        assertThat(gatewayRouteProperties.subscriptionService()).isEqualTo("lb://subscription-service");
        assertThat(gatewayRouteProperties.competitionService()).isEqualTo("lb://competition-service");
        assertThat(gatewayRouteProperties.rankingService()).isEqualTo("lb://ranking-service");
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
