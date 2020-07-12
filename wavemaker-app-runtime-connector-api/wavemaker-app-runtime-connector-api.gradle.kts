plugins {
    `java-library`
    `maven-publish`
}

group ="com.wavemaker.runtime.connector"

dependencies {
    implementation(platform(project(":wavemaker-app-runtime-services")))
}

java {
    withSourcesJar()
}

publishing {
    configurePublicationToDist(this)
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