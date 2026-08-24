plugins {
    id("com.diffplug.spotless")
    java
}

dependencies {
    "compileOnly"("org.projectlombok:lombok:1.18.38")
    "annotationProcessor"("org.projectlombok:lombok:1.18.38")
    "testCompileOnly"("org.projectlombok:lombok:1.18.38")
    "testAnnotationProcessor"("org.projectlombok:lombok:1.18.38")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat()
        importOrder("", "java", "javax", "org", "com")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
