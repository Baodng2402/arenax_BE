import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("arenax.spring-service-conventions")
}

dependencies {
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.cloud.starter.gateway.server.webmvc)
    implementation(libs.spring.cloud.starter.loadbalancer)
    implementation(libs.spring.cloud.starter.netflix.eureka.client)
}

tasks.test {
    systemProperty("eureka.client.enabled", "false")
}

// Package the canonical OpenAPI specs (docs/contracts/openapi/) as static
// resources so this service can serve them for the bundled Swagger UI page.
// docs/contracts/openapi/ stays the single source of truth; nothing here
// hand-duplicates spec content.
tasks.named<ProcessResources>("processResources") {
    from(rootProject.layout.projectDirectory.dir("docs/contracts/openapi")) {
        into("static/openapi")
    }
}
