pluginManagement {
    val springBootVersion: String by settings
    val dependencyManagementVersion: String by settings
    val spotlessVersion: String by settings
    plugins {
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version dependencyManagementVersion
        id("com.diffplug.spotless") version spotlessVersion
    }
}

rootProject.name = "cuenta-service"

include(":domain")
project(":domain").projectDir = file("./domain")

include(":application")
project(":application").projectDir = file("./application")

include(":bootstrap")
project(":bootstrap").projectDir = file("./bootstrap")

// Entry points
include(":rest-controller")
project(":rest-controller").projectDir = file("./infrastructure/entry-points/rest-controller")
include(":cliente-consumer")
project(":cliente-consumer").projectDir = file("./infrastructure/entry-points/cliente-consumer")

// Driven adapters
include(":jpa-repository")
project(":jpa-repository").projectDir = file("./infrastructure/driven-adapters/jpa-repository")
include(":redis-cache")
project(":redis-cache").projectDir = file("./infrastructure/driven-adapters/redis-cache")
include(":metrics-publisher")
project(":metrics-publisher").projectDir = file("./infrastructure/driven-adapters/metrics-publisher")
