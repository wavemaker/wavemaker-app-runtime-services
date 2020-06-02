plugins {
    `java-library`
    `maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(platform(project(":wavemaker-app-runtime-services")))
    implementation(project(":wavemaker-commons-util"))
    implementation("org.slf4j:slf4j-api")
    implementation("org.apache.commons:commons-lang3")
    implementation("commons-collections:commons-collections")
    implementation("org.springframework:spring-webmvc")
    compileOnly("javax.servlet:javax.servlet-api")
    testImplementation("junit:junit")
    testImplementation("org.springframework:spring-test")
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = project.extensions.extraProperties.get("basename") as String
            from(components["java"])
            withoutBuildIdentifier()
            pom {
                withXml {
                    updateGeneratedPom(asNode(), mapOf(
                            "compile" to configurations.implementation.get().dependencies + configurations.api.get().dependencies,
                            "provided" to configurations.compileOnly.get().dependencies
                    ))
                }
            }
        }
    }
}