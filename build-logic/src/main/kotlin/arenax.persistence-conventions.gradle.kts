plugins {
    id("arenax.spring-service-conventions")
}

dependencies {
    "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
    "implementation"("org.springframework.boot:spring-boot-starter-flyway")
    "runtimeOnly"("org.postgresql:postgresql")
    "implementation"("org.flywaydb:flyway-database-postgresql")
    "testRuntimeOnly"("com.h2database:h2")
}
