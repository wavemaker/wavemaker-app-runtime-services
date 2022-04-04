plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(enforcedPlatform(appDependenciesLibs.boms.springFramework.get()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.jackson.get()))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimeCommons)
    implementation(projects.wavemakerToolsApidocsCore)
    implementation(appDependenciesLibs.commons.collections4)
    implementation(appDependenciesLibs.commons.io)
    implementation(appDependenciesLibs.commons.lang3)
    implementation(appDependenciesLibs.commons.text)
    implementation(appDependenciesLibs.spring.context)
    implementation(appDependenciesLibs.spring.web)
    implementation(appDependenciesLibs.spring.webmvc)
    implementation(appDependenciesLibs.jackson.annotations)
    implementation(appDependenciesLibs.apache.httpclient)
    implementation(appDependenciesLibs.javax.annotation.api)
    implementation(appDependenciesLibs.javax.validationApi)
    implementation(appDependenciesLibs.feign.core)
    compileOnly(appDependenciesLibs.javax.servlet.api)
    testImplementation(appDependenciesLibs.test.junit4)
    testImplementation(appDependenciesLibs.test.mockito.all)
    testImplementation(appDependenciesLibs.javax.servlet.api)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}