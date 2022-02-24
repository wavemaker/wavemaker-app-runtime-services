plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(enforcedPlatform(libs.boms.springFramework.get()))
    implementation(projects.wavemakerCommonsUtil)
    implementation(libs.slf4j.api)
    implementation(libs.commons.lang3)
    implementation(libs.commons.collections4)
    implementation(libs.spring.webmvc)
    compileOnly(libs.javax.servlet.api)
    testImplementation(libs.test.junit4)
    testImplementation(libs.test.spring.test)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}