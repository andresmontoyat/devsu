// Composite root: one command builds and tests both services.
val services = listOf("cliente-service", "cuenta-service")

listOf("build", "clean", "test", "spotlessApply", "spotlessCheck").forEach { taskName ->
    tasks.register(taskName) {
        group = "devsu"
        description = "Runs '$taskName' on every service."
        dependsOn(services.map { gradle.includedBuild(it).task(":$taskName") })
    }
}
