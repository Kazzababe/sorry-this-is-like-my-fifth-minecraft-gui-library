rootProject.name = "sorry-this-is-like-my-fifth-minecraft-gui-library"

if (System.getenv("JITPACK").isNullOrBlank()) {
    include("example")
}
include("ravioli-gui")
