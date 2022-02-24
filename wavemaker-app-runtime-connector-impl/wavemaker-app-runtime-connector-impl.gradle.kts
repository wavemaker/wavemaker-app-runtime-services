plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime.connector"

dependencies {
    implementation(enforcedPlatform(libs.boms.springFramework.get()))
    implementation(libs.slf4j.api)
    implementation(libs.spring.context)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}