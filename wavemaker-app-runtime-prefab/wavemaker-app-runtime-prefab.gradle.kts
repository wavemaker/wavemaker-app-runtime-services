plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(enforcedPlatform(appDependencies.boms.springFramework.get()))
    implementation(projects.wavemakerCommonsUtil)
    implementation(appDependencies.slf4j.api)
    implementation(appDependencies.commons.lang3)
    implementation(appDependencies.commons.collections4)
    implementation(appDependencies.spring.webmvc)
    compileOnly(appDependencies.javax.servlet.api)
    testImplementation(appDependencies.test.junit4)
    testImplementation(appDependencies.test.spring.test)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}