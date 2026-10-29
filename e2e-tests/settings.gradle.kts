pluginManagement {
    val spotlessVersion: String by settings
    plugins {
        id("com.diffplug.spotless") version spotlessVersion
    }
}

rootProject.name = "e2e-tests"
