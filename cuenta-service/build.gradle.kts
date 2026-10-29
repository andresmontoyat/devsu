import com.diffplug.gradle.spotless.SpotlessExtension
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    java
    jacoco
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
    id("com.diffplug.spotless")
}

allprojects {
    group = "com.devsu.bank"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    // NOTE: org.springframework.boot is applied only to :bootstrap, the single BootJar producer.
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "jacoco")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${property("spring.boot.version")}")
        }
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testImplementation"("org.assertj:assertj-core")
        "testImplementation"("org.mockito:mockito-junit-jupiter")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-parameters")
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
        testLogging { events("passed", "skipped", "failed") }
    }

    val jacocoExclusions = listOf("**/config/**", "**/*Application*")
    // Domain and use cases carry the business rules: they get the stricter gate (gradle.md §4.3).
    val minimumLineCoverage = if (name in setOf("domain", "application")) "0.90" else "0.80"

    tasks.named<JacocoReport>("jacocoTestReport") {
        classDirectories.setFrom(files(classDirectories.files.map { fileTree(it) { exclude(jacocoExclusions) } }))
        reports {
            xml.required = true
            html.required = true
            // CSV feeds the per-module coverage table of the CI summary.
            csv.required = true
        }
    }

    tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        dependsOn(tasks.named("test"))
        classDirectories.setFrom(files(classDirectories.files.map { fileTree(it) { exclude(jacocoExclusions) } }))
        violationRules {
            rule {
                limit {
                    counter = "LINE"
                    minimum = minimumLineCoverage.toBigDecimal()
                }
            }
        }
    }

    tasks.named("check") {
        dependsOn(tasks.named("jacocoTestCoverageVerification"))
    }

    configure<SpotlessExtension> {
        java {
            googleJavaFormat(property("google.java.format.version") as String)
            target("src/**/*.java")
            trimTrailingWhitespace()
            endWithNewline()
            removeUnusedImports()
        }
    }
}

project(":bootstrap") {
    apply(plugin = "org.springframework.boot")
}

// Lets the repository root drive the whole service through one task path (composite build).
listOf("assemble", "build", "clean", "test", "spotlessApply", "spotlessCheck").forEach { taskName ->
    tasks.named(taskName) { dependsOn(subprojects.map { "${it.path}:$taskName" }) }
}
