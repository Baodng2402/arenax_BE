plugins {
    id("arenax.spring-service-conventions")
}

dependencies {
    implementation(libs.spring.cloud.starter.gateway.server.webmvc)
}
