plugins {
    id("arenax.persistence-conventions")
}

dependencies {
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.cloud.starter.openfeign)
    implementation(libs.spring.cloud.starter.netflix.eureka.client)
}

tasks.test {
    systemProperty("eureka.client.enabled", "false")
}
