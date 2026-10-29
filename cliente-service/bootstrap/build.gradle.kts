dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":rest-controller"))
    implementation(project(":scheduler-task"))
    implementation(project(":jpa-repository"))
    implementation(project(":kafka-producer"))
    implementation(project(":bcrypt-adapter"))
    implementation(project(":metrics-publisher"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-kafka")
    testImplementation("com.tngtech.archunit:archunit-junit5:${property("archunit.version")}")
    testImplementation("io.karatelabs:karate-junit6:${property("karate.version")}")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("${rootProject.name}.${archiveExtension.get()}")
    enabled = true
}

tasks.getByName<Jar>("jar") {
    enabled = false
}
