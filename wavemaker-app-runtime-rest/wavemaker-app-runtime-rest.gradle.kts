plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(enforcedPlatform(appDependenciesLibs.boms.springFramework.get().toString()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.jackson.get().toString()))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimeCommons)
    implementation(projects.wavemakerToolsApidocsCore)
    implementation(projects.wavemakerAppModels)
    implementation(appDependenciesLibs.commons.collections4)
    implementation(appDependenciesLibs.commons.io)
    implementation(appDependenciesLibs.commons.lang3)
    implementation(appDependenciesLibs.commons.text)
    implementation(appDependenciesLibs.spring.context)
    implementation(appDependenciesLibs.spring.web)
    implementation(appDependenciesLibs.spring.webmvc)
    implementation(appDependenciesLibs.jackson.annotations)
    implementation(appDependenciesLibs.apache.httpclient)
    implementation(appDependenciesLibs.jakarta.annotation.api)
    implementation(appDependenciesLibs.jakarta.validationApi)
    implementation(appDependenciesLibs.feign.core)
    compileOnly(appDependenciesLibs.jakarta.servlet.api)
    testImplementation(appDependenciesLibs.test.junit4)
    testImplementation(appDependenciesLibs.test.mockito.all)
    testImplementation(appDependenciesLibs.jakarta.servlet.api)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}