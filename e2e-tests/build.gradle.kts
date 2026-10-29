plugins {
    java
    id("com.diffplug.spotless")
}

group = "com.devsu.bank"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // Only the test versions are borrowed from the Boot BOM; this build has no Spring code.
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:${property("spring.boot.version")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("io.karatelabs:karate-junit6:${property("karate.version")}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// The suite needs the compose stack running, so it never runs as part of `build`.
tasks.test {
    enabled = false
}

tasks.register<Test>("e2eTest") {
    description = "Runs the use cases of the exercise against the docker compose stack."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform()
    systemProperty("e2e.clienteUrl", providers.environmentVariable("E2E_CLIENTE_URL").getOrElse("http://localhost:8081/api"))
    systemProperty("e2e.cuentaUrl", providers.environmentVariable("E2E_CUENTA_URL").getOrElse("http://localhost:8082/api"))
    // Always hits the live stack: a cached "up to date" result would prove nothing.
    outputs.upToDateWhen { false }
    testLogging { events("passed", "skipped", "failed") }
}

spotless {
    java {
        googleJavaFormat(property("google.java.format.version") as String)
        target("src/**/*.java")
        trimTrailingWhitespace()
        endWithNewline()
        removeUnusedImports()
    }
}
