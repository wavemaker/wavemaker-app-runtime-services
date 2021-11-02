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
    implementation(platform(projects.wavemakerAppRuntimeServices))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimePrefab)
    implementation(projects.wavemakerAppRuntimeConnectorAppIntegration)
    implementation(projects.wavemakerToolsApidocsCore)
    implementation(libs.commons.io)
    implementation(libs.commons.text)
    implementation(libs.commons.lang3)
    implementation(libs.commons.fileupload)
    implementation(libs.commons.collections4)
    implementation(libs.guava)
    implementation(libs.spring.web)
    implementation(libs.spring.webmvc)
    implementation(libs.spring.data.jpa)
    implementation(libs.spring.security.core)
    implementation(libs.spring.security.web)
    implementation(libs.spring.session.core)
    implementation(libs.hibernate.core)
    implementation(libs.hibernate.validator)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.datatype.hibernate5)
    implementation(libs.apache.httpclient)
    implementation(libs.tika.core)
    implementation(libs.poi)
    implementation(libs.poiOoxml) {
        exclude("com.github.virtuald", "curvesapi")
    }
    implementation(libs.antisamy) {
        exclude("org.slf4j", "slf4j-simple")
    }
    implementation(libs.freemarker)
    implementation(libs.swagger.annotations)
    implementation(libs.javax.annotation.api)
    implementation(libs.javax.jaxws.api)
    compileOnly(libs.javax.servlet.api)
    compileOnly(libs.spring.contextSupport)
    compileOnly(libs.spring.security.cas)
    compileOnly(libs.spring.security.ldap)
    compileOnly(libs.spring.security.oauth2.client)
    compileOnly(libs.spring.security.oauth2.jose)
    //TODO Need to add xalan and xml-apis exclusions for saml2-core
    compileOnly(libs.springSecuritySaml2Core)
    compileOnly(libs.spring.session.jdbc)
    runtimeOnly(libs.hikariCP)
    runtimeOnly(libs.spring.security.config)
    runtimeOnly(libs.aspectjrt)
    runtimeOnly(libs.aspectjweaver)
    testImplementation(libs.test.testng)
    testImplementation(libs.test.junit4)
    testImplementation(libs.test.mockito.all)
    testImplementation(libs.test.jsonassert)
    testImplementation(libs.javax.servlet.api)
    runtimeOnly(libs.antlr4Runtime)
    // The below dependency is adding antlr4 and its transitive dependencies as well, removing them using exclude for runtime configuration
    antlr(libs.antlr4)

    //Logging related dependencies
    implementation(libs.slf4j.api)
    implementation(libs.log4j.core)
    loggingCapabilityConfiguration(libs.log4j.slf4j.impl)

    //runtime dependencies lib
    runtimeLibDependencies(projects.wavemakerAppRuntimeCore)
    //TODO: To support DB2 implementation, the custom code needs to be updated as per the new hibernate library code
    /*runtimeLibDependencies(libs.hibernate.core) {
        version {
            strictly("5.2.17.WM")
        }
    }*/
}

configurations {
    runtimeOnly {
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
        // Added these below task dependencies as the processResources depends on those too be finished first
        // TODO need to remove the generation of app-runtime-dependencies.txt file, need to solve the use case in a better way
        dependsOn(":wavemaker-commons-util:jar")
        dependsOn(":wavemaker-app-runtime-prefab:jar")
        dependsOn(":wavemaker-tools-apidocs-core:jar")
        dependsOn(":wavemaker-app-runtime-connector-app-integration:jar")
        dependsOn(":wavemaker-app-runtime-connector-api:jar")
    }
    withType<Jar>().configureEach { // This is added to make sourcesJar task depend on generateGrammarSource task
        dependsOn(generateGrammarSource)
    }
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
    scopeMapping.get("compile")?.add(loggingCapabilityConfiguration.name)
}