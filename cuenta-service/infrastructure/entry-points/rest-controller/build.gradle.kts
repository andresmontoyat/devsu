plugins {
    id("org.openapi.generator")
}

// Contract-first (api-design.md §19): the controller implements interfaces generated from this file.
val contract = layout.projectDirectory.file("src/main/resources/openapi/cuenta-service.yaml")
val generatedDir = layout.buildDirectory.dir("generated/openapi")
val generatedPackage = "com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.generated"

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set(contract.asFile.path)
    outputDir.set(generatedDir.get().asFile.path)
    apiPackage.set("$generatedPackage.api")
    modelPackage.set("$generatedPackage.model")
    // Only interfaces and models: no supporting files, no application class.
    globalProperties.set(mapOf("apis" to "", "models" to ""))
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "useTags" to "true",
            "useSpringBoot3" to "true",
            "useJakartaEe" to "true",
            "useBeanValidation" to "true",
            "openApiNullable" to "false",
            "annotationLibrary" to "none",
            "documentationProvider" to "none",
            // Paths in the contract are relative to the /api context path, not to a generated base path.
            "requestMappingMode" to "none",
            "useResponseEntity" to "true",
            "sourceFolder" to "src/main/java",
            "hideGenerationTimestamp" to "true",
        ),
    )
    // Merge patch keeps the raw JSON: null and unknown members are rejected by CuentaPatchMapper.
    schemaMappings.set(mapOf("CuentaPatch" to "tools.jackson.databind.JsonNode"))
    // Movements are stamped as UTC instants, as in the domain.
    typeMappings.set(mapOf("OffsetDateTime" to "java.time.Instant"))
}

sourceSets.main {
    java.srcDir(generatedDir.map { it.dir("src/main/java") })
}

tasks.compileJava {
    dependsOn(tasks.openApiGenerate)
}

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}
