plugins {
    `maven-publish`
    id("io.papermc.paperweight.userdev") version("1.7.0")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")

    annotationProcessor("org.projectlombok:lombok:1.18.30")

    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
}

publishing {
    publications {
        register<MavenPublication>("devBundle") {
            from(components["java"])

            artifact(tasks.reobfJar)
        }
    }
}