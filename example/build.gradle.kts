plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

dependencies {
    implementation(project(":ravioli-gui"))
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        archiveClassifier.set("")
    }
}