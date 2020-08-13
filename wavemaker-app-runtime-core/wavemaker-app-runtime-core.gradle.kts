plugins {
    `java-library`
    `maven-publish`
}

group ="com.wavemaker.runtime"

val loggingCapabilityConfiguration by configurations.creating
val runtimeLibDependencies by configurations.creating

dependencies {
    implementation(platform(project(":wavemaker-app-runtime-services")))
    implementation(project(":wavemaker-commons-util"))
    implementation(project(":wavemaker-app-runtime-prefab"))
    implementation("com.wavemaker.tools.apidocs:wavemaker-tools-apidocs-core")
    implementation("commons-io:commons-io")
    implementation("commons-lang:commons-lang")
    implementation("org.apache.commons:commons-lang3")
    implementation("commons-fileupload:commons-fileupload")
    implementation("com.google.guava:guava")
    implementation("org.springframework:spring-orm")
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.security:spring-security-web")
    implementation("org.hibernate:hibernate-core")
    implementation("org.hibernate:hibernate-validator")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-hibernate5")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.apache.httpcomponents:httpclient")
    implementation("rome:rome:0.9")
    implementation("org.apache.poi:poi:3.17")
    implementation("org.apache.poi:poi-ooxml:3.17") {
        exclude("com.github.virtuald", "curvesapi")
    }
    implementation("org.apache.tika:tika-core:1.17")
    implementation("net.sf.jmimemagic:jmimemagic:0.1.5")
    implementation("org.freemarker:freemarker:2.3.23")
    implementation("org.owasp.antisamy:antisamy:1.5.8")
    compileOnly("javax.servlet:javax.servlet-api")
    compileOnly("org.springframework:spring-context-support")
    implementation("org.springframework.data:spring-data-commons")
    compileOnly("org.springframework.security:spring-security-ldap")
    compileOnly("org.springframework.security:spring-security-cas")
    compileOnly("org.springframework.security.extensions:spring-security-saml2-core")
    compileOnly("org.springframework.social:spring-social-security:1.1.5.RELEASE")
    compileOnly("net.sf.jasperreports:jasperreports:6.5.1") {
        //TODO need to revisit this
        exclude(group = "com.lowagie")
    }
    compileOnly("org.quartz-scheduler:quartz:2.2.3")
    runtimeOnly("com.zaxxer:HikariCP")
    runtimeOnly("org.springframework.security:spring-security-config") {
        because("Without this dependency namespaces in xml definitions were not working and ultimately deployment failed")
    }
    testImplementation("junit:junit")
    testImplementation("org.testng:testng")

    //Logging related dependencies
    implementation("org.slf4j:slf4j-api")
    implementation("log4j:log4j:1.2.17")
    loggingCapabilityConfiguration("org.slf4j:slf4j-log4j12:1.7.29")

    //runtime dependencies lib
    runtimeLibDependencies(project(":wavemaker-app-runtime-core"))
    runtimeLibDependencies(loggingCapabilityConfiguration)
}

java {
    withSourcesJar()
}


tasks {
    processResources {
        inputs.files(configurations.runtimeClasspath.get().files)
        doLast {
            var content : String = ""
            project.configurations.runtimeClasspath.get().resolvedConfiguration.resolvedArtifacts.forEach {
                content+=it.moduleVersion.toString()+"\n"
            }

            val resourcesDir : File = sourceSets.main.get().output.resourcesDir!!
            resourcesDir.mkdirs()
            File(resourcesDir, "app-runtime-dependencies.txt").writeText(content)
        }
    }
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
                            "compile" to configurations.implementation.get().dependencies + configurations.api.get().dependencies + loggingCapabilityConfiguration.dependencies,
                            "provided" to configurations.compileOnly.get().dependencies,
                            "runtime" to configurations.runtimeOnly.get().dependencies
                    ))
                }
            }
        }
    }
}