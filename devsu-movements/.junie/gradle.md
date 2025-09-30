# Gradle – Project Build and Quality Plugins

This guide documents how we use Gradle in this project and the quality plugins we rely on. The project uses Gradle 9.0.0 with Kotlin DSL.

---

## 1) Generated Gradle files and folders

Use Gradle version: 9.0.0

Generated files:
```
settings.gradle.kts
build.gradle.kts
gradle.properties
gradlew
gradlew.bat
```

Generated folder:
```
/gradle
```

---

## 2) Plugins: Spotless (format), JaCoCo (coverage), SonarQube (code quality)

Below are the key plugins we use across modules. See root `build.gradle.kts` for concrete versions.

### 2.1 Spotless (code formatting)

- Plugin: `id("com.diffplug.spotless")`
- Purpose: Enforce code style, ktlint, import ordering, trailing whitespace, and newline rules.
- Applied in: root and subprojects.

Basic Kotlin configuration example:

```kotlin
plugins {
    id("com.diffplug.spotless") version "7.2.1"
}

spotless {
    kotlin {
        // KtLint rules
        ktlint()

        // Sort imports in groups: co.edut.udem, java|javax, org, then everything else (#)
        // NOTE: adjust the first group to your base package, e.g., "co.edu.udem" in this project.
        importOrder("co.edut.udem", "java|javax", "org", "\\#")

        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        ktlint()
        importOrder("co.edut.udem", "java|javax", "org", "\\#")
    }
}
```

Notes:
- `java|javax` means both Java and Javax imports are grouped together.
- `"\\#"` is a special marker for “everything else” imports; backslash-escaped for Kotlin strings.
- If you prefer configuring import order via ktlint/.editorconfig, add:

```properties
# .editorconfig
[*.{kt,kts}]
ij_kotlin_imports_layout=co.edu.udem,java,javax,org,*,^
```

Use either Spotless importOrder or the .editorconfig layout (not both) to avoid conflicts.

### 2.2 JaCoCo (test coverage)

- Plugin: `id("jacoco")`
- Purpose: Generate coverage reports per module and aggregate them if needed.

Example configuration (per-module):

```kotlin
plugins {
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("test")
    reports {
        xml.required = true
        csv.required = true
        html.required = false
    }
    // Optionally exclude generated or config code
    afterEvaluate {
        classDirectories.setFrom(
            files(
                classDirectories.files.map {
                    fileTree(it) {
                        exclude("**/config/**")
                    }
                }
            )
        )
    }
}
```

For a multi-module aggregate report, create a root task that collects `executionData`, `classDirectories`, and `sourceDirectories` from subprojects (see root build file in this repo for a working example named `codeCoverageReport`).

### 2.3 SonarQube (static analysis)

- Plugin: `id("org.sonarqube")`
- Purpose: Push analysis to SonarCloud/SonarQube server with JaCoCo XML coverage.

Example Sonar settings (SonarCloud):

```kotlin
plugins { id("org.sonarqube") version "6.3.1.5724" }

sonar {
    properties {
        property("sonar.projectKey", "integral-software_ms-indumil-employees-api")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "integral-software")
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.gradle.skipCompile", "true")
        property("sonar.kotlin.file.suffixes", ".kt")
        // Collect XML reports from all submodules
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            subprojects.joinToString(",") { "$projectDir/${'$'}{it.name}/build/reports/jacoco/test/jacocoTestReport.xml" }
        )
        property("sonar.coverage.exclusions", "**/config/**,")
        property("sonar.cpd.exclusions", "**/*Config*")
    }
}
```

---

## 3) Gradle support (scaffolding)

Generate all Gradle resources for a new project:

Generated files:
```
settings.gradle.kts
build.gradle.kts
gradle.properties
gradlew
gradlew.bat
```

Generated folder:
```
/gradle
```

---

## 4) Git

Generate a `.gitignore` file. Suggested ignores:
- Ignore IDEA/IntelliJ resources
- Ignore Java/Kotlin compiled classes
- Ignore Gradle output resources
- Ignore Maven output resources
- Ignore package manager caches
- Ignore logs and temporary files

---

## Appendix: Framework-agnostic Samples

This appendix provides neutral, copy-pasteable snippets that you can adapt to any Kotlin JVM project (not tied to Spring or any specific framework). Replace placeholders like your.base.package and values in sonar.* with your own.

### A1. Spotless (Kotlin) – generic import order and ktlint

```kotlin
plugins {
    id("com.diffplug.spotless") version "7.2.1"
}

spotless {
    kotlin {
        ktlint()
        // Use your base package first, then Java/Javax, then org, then everything else (#)
        importOrder("your.base.package", "java|javax", "org", "\\#")
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        ktlint()
        importOrder("your.base.package", "java|javax", "org", "\\#")
    }
}
```

Alternative via .editorconfig:

```properties
# .editorconfig
[*.{kt,kts}]
ij_kotlin_imports_layout=your.base.package,java,javax,org,*,^
```

### A2. JaCoCo – minimal per-module config

```kotlin
plugins { id("jacoco") }

jacoco { toolVersion = "0.8.12" }

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("test")
    reports { xml.required = true; csv.required = true; html.required = false }
}
```

### A3. SonarQube – generic SonarCloud example

```kotlin
plugins { id("org.sonarqube") version "6.3.1.5724" }

sonar {
    properties {
        property("sonar.projectKey", "your_org_your_project_key")
        property("sonar.organization", "your_org")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.kotlin.file.suffixes", ".kt")
        // Collect XML reports from all submodules
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            subprojects.joinToString(",") { "$projectDir/${'$'}{it.name}/build/reports/jacoco/test/jacocoTestReport.xml" }
        )
        // Example exclusions (customize as needed)
        property("sonar.coverage.exclusions", "**/config/**,")
        property("sonar.cpd.exclusions", "**/*Config*")
    }
}
```
