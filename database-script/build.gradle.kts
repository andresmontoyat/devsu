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
    // Versions come from the Boot BOM, so Liquibase and the driver match what the services run.
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:${property("spring.boot.version")}"))
    testImplementation("org.liquibase:liquibase-core")
    testImplementation("org.postgresql:postgresql")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.slf4j:slf4j-nop")
}

val repositoryRoot = rootDir.parentFile
val changelogs = mapOf(
    "basedatos.changelog.cliente" to "cliente-service/infrastructure/driven-adapters/jpa-repository/src/main/resources",
    "basedatos.changelog.cuenta" to "cuenta-service/infrastructure/driven-adapters/jpa-repository/src/main/resources",
)
val script = repositoryRoot.resolve("BaseDatos.sql")

tasks.test {
    useJUnitPlatform()
    changelogs.forEach { (key, path) ->
        systemProperty(key, repositoryRoot.resolve(path).absolutePath)
        inputs.dir(repositoryRoot.resolve(path)).withPropertyName(key)
    }
    systemProperty("basedatos.script", script.absolutePath)
    inputs.file(script).withPropertyName("basedatos.script")
    testLogging { events("passed", "skipped", "failed") }
}

// BaseDatos.sql is a deliverable derived from the Liquibase changelogs, never edited by hand.
tasks.register<JavaExec>("generateBaseDatos") {
    description = "Regenerates BaseDatos.sql from the Liquibase changelogs of both services (needs Docker)."
    group = "devsu"
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.devsu.bank.basedatos.BaseDatosGenerator")
    changelogs.forEach { (key, path) -> systemProperty(key, repositoryRoot.resolve(path).absolutePath) }
    systemProperty("basedatos.script", script.absolutePath)
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
