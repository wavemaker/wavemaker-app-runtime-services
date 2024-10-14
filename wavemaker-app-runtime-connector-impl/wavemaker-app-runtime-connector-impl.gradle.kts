plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime.connector"

dependencies {
    implementation(enforcedPlatform(appDependenciesLibs.boms.slf4j.get().toString()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.springFramework.get().toString()))
    implementation(appDependenciesLibs.slf4j.api)
    implementation(appDependenciesLibs.spring.context)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}