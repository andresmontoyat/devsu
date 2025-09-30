
plugins {
    java
    application
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "7.2.1"
    id("jacoco")
}

group = "com.devsu.customer"
version = "0.0.1"
description = "devsu-customers"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "application")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "jacoco")

    java.sourceCompatibility = JavaVersion.VERSION_21
    java.targetCompatibility = JavaVersion.VERSION_21

    dependencies {
        implementation("org.slf4j:slf4j-api")
        implementation("com.fasterxml.jackson.core:jackson-databind")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
        implementation("org.projectlombok:lombok:1.18.36")
        implementation("org.mapstruct:mapstruct:1.6.2")

        annotationProcessor("org.projectlombok:lombok:1.18.36")
        annotationProcessor("org.mapstruct:mapstruct-processor:1.6.2")
        annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

        implementation("org.slf4j:slf4j-api:2.0.16")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.3")
        }
    }

    tasks.getByName<JacocoReport>("jacocoTestReport") {
        dependsOn("test")

        reports {
            xml.required = true
            csv.required = true
        }

        afterEvaluate {
            classDirectories.setFrom(
                files(
                    classDirectories.files.map {
                        fileTree(it) {
                            exclude(
                                "**/config/**",
                            )
                        }
                    }
                )
            )
        }
    }

    tasks.getByName<Jar>("bootJar") {
        enabled = false
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }

    tasks.getByName<Jar>("jar") {
        enabled = true
    }

    spotless {
        java {
            palantirJavaFormat()
            removeUnusedImports()
            removeWildcardImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}

tasks.getByName<Jar>("bootJar") {
    enabled = false
}


tasks.register<JacocoReport>("codeCoverageReport") {
    description = "Generates the HTML documentation for this project."
    group = JavaBasePlugin.VERIFICATION_GROUP

    val excludedPatterns = listOf(
        "**/config/**"
    )

    val allClassDirs = files()
    val allSourceDirs = files()
    val allExecData = files()

    subprojects {
        plugins.withType<JacocoPlugin>().configureEach {
            // Exec data
            tasks.withType<Test>().configureEach {
                val jacocoExt = extensions.findByType<JacocoTaskExtension>()
                if (jacocoExt != null && jacocoExt.destinationFile != null) {
                    allExecData.from(jacocoExt.destinationFile)
                }
            }

            // SourceSet info
            val sourceSets = extensions.getByType<SourceSetContainer>()
            val mainSourceSet = sourceSets.findByName("main") ?: return@configureEach

            allSourceDirs.from(mainSourceSet.allSource.srcDirs)

            val filteredClasses = mainSourceSet.output.classesDirs.asFileTree.matching {
                exclude(excludedPatterns)
            }
            allClassDirs.from(filteredClasses)
        }
    }

    classDirectories.setFrom(allClassDirs)
    sourceDirectories.setFrom(allSourceDirs)
    executionData.setFrom(allExecData)

    reports {
        xml.required = true
        html.required = true

    }
}

