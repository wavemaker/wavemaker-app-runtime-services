plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

val loggingCapabilityConfiguration: Configuration by configurations.creating
val runtimeLibDependencies: Configuration by configurations.creating {
    extendsFrom(loggingCapabilityConfiguration)
}

dependencies {
    implementation(enforcedPlatform(appDependenciesLibs.boms.springFramework.get().toString()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.springSecurity.get().toString()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.springData.get().toString()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.springSession.get().toString()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.log4j.get().toString()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.jackson.get().toString()))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimeCommons)
    implementation(projects.wavemakerAppRuntimePrefab)
    implementation(projects.wavemakerAppRuntimeRest)
    implementation(projects.wavemakerAppRuntimeSoap)
    implementation(projects.wavemakerAppRuntimeDb)
    implementation(projects.wavemakerAppRuntimeConnectorAppIntegration)
    implementation(projects.wavemakerToolsApidocsCore)
    implementation(appDependenciesLibs.commons.collections4)
    implementation(appDependenciesLibs.commons.text)
    implementation(appDependenciesLibs.guava)
    implementation(appDependenciesLibs.spring.webmvc)
    implementation(appDependenciesLibs.spring.data.jpa)
    implementation(appDependenciesLibs.spring.security.core)
    implementation(appDependenciesLibs.spring.security.web)
    implementation(appDependenciesLibs.spring.session.core)
    implementation(appDependenciesLibs.hibernate.validator)
    implementation(appDependenciesLibs.jackson.dataformat.yaml)
    implementation(appDependenciesLibs.jackson.datatype.jsr310)
    implementation(appDependenciesLibs.jackson.datatype.hibernate5)
    implementation(appDependenciesLibs.apache.httpclient)
    implementation(appDependenciesLibs.tika.core)
    compileOnly(appDependenciesLibs.opensaml.saml.impl)
    compileOnly(appDependenciesLibs.opensaml.core)
    compileOnly(appDependenciesLibs.opensaml.api)
    implementation(appDependenciesLibs.antisamy) {
        exclude("org.slf4j", "slf4j-simple")
    }
    implementation(appDependenciesLibs.wordnik.swagger.annotations)
    implementation(appDependenciesLibs.javax.annotation.api)
    compileOnly(appDependenciesLibs.javax.servlet.api)
    compileOnly(appDependenciesLibs.spring.contextSupport)
    compileOnly(appDependenciesLibs.spring.security.cas)
    compileOnly(appDependenciesLibs.spring.security.ldap)
    compileOnly(appDependenciesLibs.spring.security.oauth2.client)
    compileOnly(appDependenciesLibs.spring.security.oauth2.jose)
    compileOnly(appDependenciesLibs.spring.security.oauth2.resource.server)
    compileOnly(appDependenciesLibs.spring.security.saml2.service.provider)
    compileOnly(appDependenciesLibs.spring.session.jdbc)
    compileOnly(appDependenciesLibs.poiOoxml) {
        because("Needed this for cleaning up memory references in Cleanupistener. " +
                "TODO need to remove this dependency")
    }
    compileOnly(appDependenciesLibs.hibernate.core) {
        because("Used for getting roles for logged in user. Used in conjuction with database service in the project." +
                "TODO need to remove this dependency.")
    }
    runtimeOnly(appDependenciesLibs.spring.security.config)
    runtimeOnly(appDependenciesLibs.commons.fileupload)
    testImplementation(appDependenciesLibs.test.junit4)

    //Logging related dependencies
    implementation(appDependenciesLibs.slf4j.api)
    implementation(appDependenciesLibs.log4j.core)
    loggingCapabilityConfiguration(appDependenciesLibs.log4j.slf4j.impl)

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