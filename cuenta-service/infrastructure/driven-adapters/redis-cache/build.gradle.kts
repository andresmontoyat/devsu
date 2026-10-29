dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:${property("resilience4j.version")}")

    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
}
