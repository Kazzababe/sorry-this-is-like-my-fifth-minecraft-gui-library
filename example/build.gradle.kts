plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("io.papermc.paperweight.userdev") version("1.7.0")
}

repositories {
    mavenLocal()
}

dependencies {
    implementation(project(":ravioli-gui"))

    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
}

tasks {
    build {
        dependsOn("reobfJar")
    }
}