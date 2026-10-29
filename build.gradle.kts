// Composite root: one command builds and tests both services.
val services = listOf("cliente-service", "cuenta-service")
// database-script runs in `build` too: its test fails when BaseDatos.sql drifts from the changelogs.
val verified = services + "database-script"
val formatted = verified + "e2e-tests"

listOf("assemble", "build", "clean", "test").forEach { taskName ->
    tasks.register(taskName) {
        group = "devsu"
        description = "Runs '$taskName' on every service and on the BaseDatos.sql check."
        dependsOn(verified.map { gradle.includedBuild(it).task(":$taskName") })
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

tasks.register("generateBaseDatos") {
    group = "devsu"
    description = "Regenerates BaseDatos.sql from the Liquibase changelogs (needs Docker)."
    dependsOn(gradle.includedBuild("database-script").task(":generateBaseDatos"))
}
