pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("build-logic")
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "arenax"

include(
    "libs:messaging-foundation",
    "services:api-gateway",
    "services:identity-service",
    "services:tenant-service",
    "services:subscription-service",
    "services:competition-service",
    "services:ranking-service",
    "services:discovery-server",
)
