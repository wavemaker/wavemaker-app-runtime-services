plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime.connector"

dependencies {
    implementation(platform(projects.wavemakerAppRuntimeServices))
    implementation(projects.wavemakerAppRuntimeConnectorApi)
    implementation(projects.wavemakerCommonsUtil)
    implementation(libs.slf4j.api)
    implementation(libs.spring.beans)
    implementation(libs.spring.context)
    implementation(libs.snakeyaml)
    compileOnly(libs.javax.servlet.api)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}