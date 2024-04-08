plugins {
    java
}

allprojects {
    apply(plugin = "java")

    group = "ravioli.gravioli"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()

        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }
}