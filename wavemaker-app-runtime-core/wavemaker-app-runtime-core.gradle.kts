plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

val loggingCapabilityConfiguration: Configuration by configurations.creating
val runtimeLibDependencies: Configuration by configurations.creating {
    extendsFrom(loggingCapabilityConfiguration)
}

dependencies {
    implementation(enforcedPlatform(appDependencies.boms.springFramework.get()))
    implementation(enforcedPlatform(appDependencies.boms.springSecurity.get()))
    implementation(enforcedPlatform(appDependencies.boms.springData.get()))
    implementation(enforcedPlatform(appDependencies.boms.springSession.get()))
    implementation(enforcedPlatform(appDependencies.boms.log4j.get()))
    implementation(enforcedPlatform(appDependencies.boms.jackson.get()))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimeCommons)
    implementation(projects.wavemakerAppRuntimePrefab)
    implementation(projects.wavemakerAppRuntimeRest)
    implementation(projects.wavemakerAppRuntimeSoap)
    implementation(projects.wavemakerAppRuntimeDb)
    implementation(projects.wavemakerAppRuntimeConnectorAppIntegration)
    implementation(projects.wavemakerToolsApidocsCore)
    implementation(appDependencies.commons.collections4)
    implementation(appDependencies.commons.text)
    implementation(appDependencies.guava)
    implementation(appDependencies.spring.webmvc)
    implementation(appDependencies.spring.data.jpa)
    implementation(appDependencies.spring.security.core)
    implementation(appDependencies.spring.security.web)
    implementation(appDependencies.spring.session.core)
    implementation(appDependencies.hibernate.validator)
    implementation(appDependencies.jackson.dataformat.yaml)
    implementation(appDependencies.jackson.datatype.jsr310)
    implementation(appDependencies.jackson.datatype.hibernate5)
    implementation(appDependencies.apache.httpclient)
    implementation(appDependencies.tika.core)
    implementation(appDependencies.antisamy) {
        exclude("org.slf4j", "slf4j-simple")
    }
    implementation(appDependencies.wordnik.swagger.annotations)
    implementation(appDependencies.javax.annotation.api)
    compileOnly(appDependencies.javax.servlet.api)
    compileOnly(appDependencies.spring.contextSupport)
    compileOnly(appDependencies.spring.security.cas)
    compileOnly(appDependencies.spring.security.ldap)
    compileOnly(appDependencies.spring.security.oauth2.client)
    compileOnly(appDependencies.spring.security.oauth2.jose)
    compileOnly(appDependencies.spring.security.saml2.service.provider)
    compileOnly(appDependencies.spring.session.jdbc)
    compileOnly(appDependencies.poiOoxml) {
        because("Needed this for cleaning up memory references in Cleanupistener. " +
                "TODO need to remove this dependency")
    }
    compileOnly(appDependencies.hibernate.core) {
        because("Used for getting roles for logged in user. Used in conjuction with database service in the project." +
                "TODO need to remove this dependency.")
    }
    runtimeOnly(appDependencies.spring.security.config)
    runtimeOnly(appDependencies.commons.fileupload)
    testImplementation(appDependencies.test.junit4)

    //Logging related dependencies
    implementation(appDependencies.slf4j.api)
    implementation(appDependencies.log4j.core)
    loggingCapabilityConfiguration(appDependencies.log4j.slf4j.impl)

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