plugins {
    java
    id("io.papermc.paperweight.userdev") version("1.7.0") apply false
}

allprojects {
    apply(plugin = "java")

    group = "ravioli.gravioli"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.dmulloy2.net/repository/public/")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }
}