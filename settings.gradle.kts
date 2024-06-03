rootProject.name = "sorry-this-is-like-my-fifth-minecraft-gui-library"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

if (System.getenv("JITPACK").isNullOrBlank()) {
    include("example")
}
include("ravioli-gui")
