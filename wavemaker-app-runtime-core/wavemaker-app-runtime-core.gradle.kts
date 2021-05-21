plugins {
    `java-library-maven-publish`
    `antlr`
}

group ="com.wavemaker.runtime"

val loggingCapabilityConfiguration: Configuration by configurations.creating
val runtimeLibDependencies: Configuration by configurations.creating {
    extendsFrom(loggingCapabilityConfiguration)
}

dependencies {
    implementation(platform(project(":wavemaker-app-runtime-services")))
    implementation(project(":wavemaker-commons-util"))
    implementation(project(":wavemaker-app-runtime-prefab"))
    implementation(project(":wavemaker-app-runtime-connector-app-integration"))
    implementation(project(":wavemaker-tools-apidocs-core"))
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
    implementation("net.sf.jmimemagic:jmimemagic:0.1.5") {
        exclude("log4j", "log4j")
    }
    implementation("org.apache.tika:tika-core:1.24.1")
    implementation("org.apache.poi:poi:4.1.2")
    implementation("org.apache.poi:poi-ooxml:4.1.2") {
        exclude("com.github.virtuald", "curvesapi")
    }
    implementation("org.owasp.antisamy:antisamy:1.6.3") {
        exclude("org.slf4j", "slf4j-simple")
    }
    implementation("org.freemarker:freemarker:2.3.30")
    implementation("com.wordnik:swagger-annotations:1.3.10")
    compileOnly("javax.servlet:javax.servlet-api")
    compileOnly("org.springframework:spring-context-support")
    compileOnly("org.springframework.security:spring-security-cas")
    compileOnly("org.springframework.security:spring-security-ldap")
    compileOnly("org.springframework.security:spring-security-oauth2-client")
    compileOnly("org.springframework.security:spring-security-oauth2-jose")
    //TODO Need to add xalan and xml-apis exclusions for saml2-core
    compileOnly("org.springframework.security.extensions:spring-security-saml2-core")
    compileOnly("org.springframework.session:spring-session-jdbc")
    compileOnly("org.quartz-scheduler:quartz:2.3.2")
    runtimeOnly("com.zaxxer:HikariCP")
    runtimeOnly("org.springframework.security:spring-security-config")
    testImplementation("org.testng:testng")
    testImplementation("junit:junit")
    testImplementation("org.mockito:mockito-all")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    testImplementation("javax.servlet:javax.servlet-api")
    runtimeOnly("org.antlr:antlr4-runtime:4.8-1")
    // The below dependency is adding antlr4 and its transitive depedencies as well, removing them using exclude for runtime configuration
    antlr("org.antlr:antlr4:4.8-1")

    //Logging related dependencies
    implementation("org.slf4j:slf4j-api")
    implementation("org.apache.logging.log4j:log4j-core")
    loggingCapabilityConfiguration("org.apache.logging.log4j:log4j-slf4j-impl")

    //runtime dependencies lib
    runtimeLibDependencies(project(":wavemaker-app-runtime-core"))
    //TODO: To support DB2 implementation, the custom code needs to be updated as per the new hibernate library code
    /*runtimeLibDependencies("org.hibernate:hibernate-core") {
        version {
            strictly("5.2.17.WM")
        }
    }*/
}

configurations {
    runtime {
        exclude("org.antlr", "antlr4")
    }
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

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
    scopeMapping.get("compile")?.add(loggingCapabilityConfiguration.name)
}