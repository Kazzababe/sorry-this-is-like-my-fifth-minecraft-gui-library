plugins {
    `maven-publish`
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")

    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}