import java.text.SimpleDateFormat
import java.util.Date

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))

    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
}

tasks.getByName<Jar>("bootJar") {
    enabled = false
}

tasks.register("createMigrationFile") {
    description = "Generates liquibase migration file with current timestamp and a placeholder for the migration name."
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    doLast {
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val migrationFileName = "V${timestamp}__change_name_here.sql"
        val migrationFilePath = "src/main/resources/db/changelog/changes/$migrationFileName"
        file(migrationFilePath).createNewFile()
        println("Created migration file: $migrationFileName")
    }
}
