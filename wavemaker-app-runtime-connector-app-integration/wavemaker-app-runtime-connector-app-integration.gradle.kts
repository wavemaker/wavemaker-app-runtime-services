plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime.connector"

dependencies {
    implementation(enforcedPlatform(appDependenciesLibs.boms.slf4j.get().toString()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.springFramework.get().toString()))
    implementation(projects.wavemakerAppRuntimeConnectorApi)
    implementation(projects.wavemakerCommonsUtil)
    implementation(appDependenciesLibs.slf4j.api)
    implementation(appDependenciesLibs.spring.beans)
    implementation(appDependenciesLibs.spring.context)
    implementation(appDependenciesLibs.snakeyaml)
    compileOnly(appDependenciesLibs.jakarta.servlet.api)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}