plugins {
    `java-library`
    `antlr`
    `maven-publish`
}

group ="com.wavemaker.runtime"

val wavemakerAppRuntimeExtraDependencies by configurations.creating

dependencies {
    implementation(platform(project(":wavemaker-app-runtime-services")))
    implementation(project(":wavemaker-commons-util"))
    implementation(project(":wavemaker-app-runtime-prefab"))
    implementation(project(":wavemaker-app-runtime-connector-app-integration"))
    implementation("com.wavemaker.tools.apidocs:wavemaker-tools-apidocs-core")
    implementation("org.slf4j:slf4j-api")
    implementation("commons-io:commons-io")
    implementation("org.apache.commons:commons-text")
    implementation("org.apache.commons:commons-lang3")
    implementation("commons-fileupload:commons-fileupload")
    implementation("org.apache.commons:commons-collections4")
    implementation("com.google.guava:guava")
    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-webmvc")
    implementation("org.springframework.data:spring-data-jpa")
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.security:spring-security-web")
    implementation("org.springframework.session:spring-session-core")
    implementation("org.hibernate:hibernate-core")
    implementation("org.hibernate:hibernate-validator")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-hibernate5")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("org.apache.httpcomponents:httpclient")
    implementation("net.sf.jmimemagic:jmimemagic:0.1.5")
    implementation("org.apache.tika:tika-core:1.20")
    implementation("org.apache.poi:poi:4.0.1")
    implementation("org.apache.poi:poi-ooxml:4.0.1") {
        exclude("com.github.virtuald", "curvesapi")
    }
    implementation("org.owasp.antisamy:antisamy:1.5.8")
    implementation("org.freemarker:freemarker:2.3.28")
    implementation("rome:rome:0.9")
    implementation("log4j:log4j:1.2.17")
    compileOnly("javax.servlet:javax.servlet-api")
    compileOnly("org.springframework:spring-context-support")
    compileOnly("org.springframework.security:spring-security-cas")
    compileOnly("org.springframework.security:spring-security-ldap")
    compileOnly("org.springframework.security:spring-security-oauth2-client")
    compileOnly("org.springframework.security:spring-security-oauth2-jose")
    compileOnly("org.springframework.social:spring-social-core:1.1.5.RELEASE")
    compileOnly("org.springframework.social:spring-social-security:1.1.5.RELEASE")
    compileOnly("org.springframework.social:spring-social-web:1.1.5.RELEASE")
    //TODO Need to add xalan and xml-apis exclusions for saml2-core
    compileOnly("org.springframework.security.extensions:spring-security-saml2-core")
    compileOnly("net.sf.jasperreports:jasperreports:6.7.0") {
        //TODO need to revisit this
        exclude(group = "com.lowagie")
    }
    compileOnly("org.quartz-scheduler:quartz:2.3.0")
    runtimeOnly("com.zaxxer:HikariCP")
    runtimeOnly("org.springframework.security:spring-security-config") {
        because("Without this dependency namespaces in xml definitions were not working and ultimately deployment failed")
    }
    testImplementation("org.testng:testng")
    testImplementation("junit:junit")
    testImplementation("org.mockito:mockito-all")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    testImplementation("javax.servlet:javax.servlet-api")
    runtimeOnly("org.antlr:antlr4-runtime:4.7.2")
    // The below dependency is adding antlr4 and its transitive depedencies as well, removing them using exclude for runtime configuration
    antlr("org.antlr:antlr4:4.7.2")
    wavemakerAppRuntimeExtraDependencies("org.slf4j:slf4j-log4j12:1.7.29")
}

configurations {
    runtime {
        exclude("org.antlr", "antlr4")
    }
}

java {
    withSourcesJar()
}

tasks {
    processResources {
        inputs.files(configurations.runtimeClasspath.get().files)
        doLast {
            var content = ""
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
                        "compile" to configurations.implementation.get().dependencies + configurations.api.get().dependencies + wavemakerAppRuntimeExtraDependencies.dependencies,
                        "provided" to configurations.compileOnly.get().dependencies,
                        "runtime" to configurations.runtimeOnly.get().dependencies
                    ))
                }
            }
        }
    }
}