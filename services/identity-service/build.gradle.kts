plugins {
    id("arenax.persistence-conventions")
}

dependencies {
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.cloud.starter.netflix.eureka.client)
}

tasks.test {
    systemProperty("eureka.client.enabled", "false")
}
