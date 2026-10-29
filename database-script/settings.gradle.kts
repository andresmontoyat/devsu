pluginManagement {
    val spotlessVersion: String by settings
    plugins {
        id("com.diffplug.spotless") version spotlessVersion
    }
}

rootProject.name = "database-script"
