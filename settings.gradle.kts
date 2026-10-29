rootProject.name = "devsu-bank"

includeBuild("cliente-service")
includeBuild("cuenta-service")
// End-to-end suite: runs only through `e2eTest`, against the docker compose stack.
includeBuild("e2e-tests")
