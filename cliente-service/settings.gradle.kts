pluginManagement {
    val springBootVersion: String by settings
    val dependencyManagementVersion: String by settings
    val spotlessVersion: String by settings
    val openApiGeneratorVersion: String by settings
    plugins {
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version dependencyManagementVersion
        id("com.diffplug.spotless") version spotlessVersion
        id("org.openapi.generator") version openApiGeneratorVersion
    }
}

rootProject.name = "cliente-service"

include(":domain")
project(":domain").projectDir = file("./domain")

include(":application")
project(":application").projectDir = file("./application")

include(":bootstrap")
project(":bootstrap").projectDir = file("./bootstrap")

// Entry points
include(":rest-controller")
project(":rest-controller").projectDir = file("./infrastructure/entry-points/rest-controller")
include(":scheduler-task")
project(":scheduler-task").projectDir = file("./infrastructure/entry-points/scheduler-task")

// Driven adapters
include(":jpa-repository")
project(":jpa-repository").projectDir = file("./infrastructure/driven-adapters/jpa-repository")
include(":kafka-producer")
project(":kafka-producer").projectDir = file("./infrastructure/driven-adapters/kafka-producer")
include(":bcrypt-adapter")
project(":bcrypt-adapter").projectDir = file("./infrastructure/driven-adapters/bcrypt-adapter")
include(":metrics-publisher")
project(":metrics-publisher").projectDir = file("./infrastructure/driven-adapters/metrics-publisher")
