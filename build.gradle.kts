// Composite root: one command builds and tests both services.
val services = listOf("cliente-service", "cuenta-service")
val formatted = services + "e2e-tests"

listOf("build", "clean", "test").forEach { taskName ->
    tasks.register(taskName) {
        group = "devsu"
        description = "Runs '$taskName' on every service."
        dependsOn(services.map { gradle.includedBuild(it).task(":$taskName") })
    }
}

listOf("spotlessApply", "spotlessCheck").forEach { taskName ->
    tasks.register(taskName) {
        group = "devsu"
        description = "Runs '$taskName' on every build."
        dependsOn(formatted.map { gradle.includedBuild(it).task(":$taskName") })
    }
}

tasks.register("e2eTest") {
    group = "devsu"
    description = "Runs the use cases of the exercise against `docker compose -f docker/docker-compose.yaml up`."
    dependsOn(gradle.includedBuild("e2e-tests").task(":e2eTest"))
}
