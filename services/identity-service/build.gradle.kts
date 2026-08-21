plugins {
    id("arenax.persistence-conventions")
}

dependencies {
    implementation(project(":libs:messaging-foundation"))
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.cloud.starter.netflix.eureka.client)
}

tasks.test {
    systemProperty("eureka.client.enabled", "false")
}
