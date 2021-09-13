plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

val loggingCapabilityConfiguration: Configuration by configurations.creating
val runtimeLibDependencies: Configuration by configurations.creating {
    extendsFrom(loggingCapabilityConfiguration)
}

dependencies {
    implementation(platform(projects.wavemakerAppRuntimeServices))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimeCommons)
    implementation(projects.wavemakerAppRuntimePrefab)
    implementation(projects.wavemakerAppRuntimeRest)
    implementation(projects.wavemakerAppRuntimeSoap)
    implementation(projects.wavemakerAppRuntimeDb)
    implementation(projects.wavemakerAppRuntimeConnectorAppIntegration)
    implementation(projects.wavemakerToolsApidocsCore)
    implementation(libs.commons.collections4)
    implementation(libs.commons.text)
    implementation(libs.guava)
    implementation(libs.spring.webmvc)
    implementation(libs.spring.data.jpa)
    implementation(libs.spring.security.core)
    implementation(libs.spring.security.web)
    implementation(libs.spring.session.core)
    implementation(libs.hibernate.validator)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.datatype.hibernate5)
    implementation(libs.apache.httpclient)
    implementation(libs.tika.core)
    implementation(libs.antisamy) {
        exclude("org.slf4j", "slf4j-simple")
    }
    implementation(libs.wordnik.swagger.annotations)
    implementation(libs.javax.annotation.api)
    compileOnly(libs.javax.servlet.api)
    compileOnly(libs.spring.contextSupport)
    compileOnly(libs.spring.security.cas)
    compileOnly(libs.spring.security.ldap)
    compileOnly(libs.spring.security.oauth2.client)
    compileOnly(libs.spring.security.oauth2.jose)
    compileOnly(libs.spring.security.saml2.service.provider)
    compileOnly(libs.spring.session.jdbc)
    compileOnly(libs.poiOoxml) {
        because("Needed this for cleaning up memory references in Cleanupistener. " +
                "TODO need to remove this dependency")
    }
    compileOnly(libs.hibernate.core) {
        because("Used for getting roles for logged in user. Used in conjuction with database service in the project." +
                "TODO need to remove this dependency.")
    }
    runtimeOnly(libs.spring.security.config)
    runtimeOnly(libs.commons.fileupload)
    testImplementation(libs.test.junit4)

    //Logging related dependencies
    implementation(libs.slf4j.api)
    implementation(libs.log4j.core)
    loggingCapabilityConfiguration(libs.log4j.slf4j.impl)

    //runtime dependencies lib
    runtimeLibDependencies(projects.wavemakerAppRuntimeCore)
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
        dependsOn(":wavemaker-app-runtime-commons:jar")
        dependsOn(":wavemaker-app-runtime-prefab:jar")
        dependsOn(":wavemaker-app-runtime-db:jar")
        dependsOn(":wavemaker-app-runtime-soap:jar")
        dependsOn(":wavemaker-app-runtime-rest:jar")
        dependsOn(":wavemaker-tools-apidocs-core:jar")
        dependsOn(":wavemaker-app-runtime-connector-app-integration:jar")
        dependsOn(":wavemaker-app-runtime-connector-api:jar")
    }
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
    scopeMapping.get("compile")?.add(loggingCapabilityConfiguration.name)
}