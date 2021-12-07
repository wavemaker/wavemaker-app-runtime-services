plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(platform(projects.wavemakerAppRuntimeServices))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimePrefab)
    implementation(libs.spring.context)
    implementation(libs.spring.web)
    implementation(libs.jackson.dataformat.yaml)
    compileOnly(libs.javax.servlet.api)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}