dependencies {
    implementation(project(":application"))

    implementation("org.springframework:spring-core")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.security:spring-security-crypto")
}

tasks.getByName<Jar>("bootJar") {
    enabled = false
}
