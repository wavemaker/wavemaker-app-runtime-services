plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(enforcedPlatform(appDependenciesLibs.boms.springFramework.get().toString()))
    implementation(projects.wavemakerCommonsUtil)
    implementation(appDependenciesLibs.slf4j.api)
    implementation(appDependenciesLibs.commons.lang3)
    implementation(appDependenciesLibs.commons.collections4)
    implementation(appDependenciesLibs.spring.webmvc)
    compileOnly(appDependenciesLibs.javax.servlet.api)
    testImplementation(appDependenciesLibs.test.junit4)
    testImplementation(appDependenciesLibs.test.spring.test)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}