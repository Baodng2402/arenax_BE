plugins {
    id("arenax.spring-service-conventions")
}

dependencies {
    implementation(libs.spring.cloud.starter.gateway.server.webmvc)
    implementation(libs.spring.cloud.starter.loadbalancer)
    implementation(libs.spring.cloud.starter.netflix.eureka.client)
}

tasks.test {
    systemProperty("eureka.client.enabled", "false")
}
