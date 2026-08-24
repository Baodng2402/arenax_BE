plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("com.diffplug.spotless:com.diffplug.spotless.gradle.plugin:8.10.0")
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:4.0.6")
    implementation("io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:1.1.7")
}
