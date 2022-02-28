plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime.connector"

dependencies {
    implementation(enforcedPlatform(appDependencies.boms.springFramework.get()))
    implementation(appDependencies.slf4j.api)
    implementation(appDependencies.spring.context)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}