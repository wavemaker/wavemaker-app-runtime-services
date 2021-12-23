plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(platform(projects.wavemakerAppRuntimeServices))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimeCommons)
    implementation(projects.wavemakerToolsApidocsCore)
    implementation(libs.commons.collections4)
    implementation(libs.commons.io)
    implementation(libs.commons.lang3)
    implementation(libs.commons.text)
    implementation(libs.spring.context)
    implementation(libs.spring.web)
    implementation(libs.spring.webmvc)
    implementation(libs.jackson.annotations)
    implementation(libs.apache.httpclient)
    implementation(libs.javax.annotation.api)
    implementation(libs.javax.validationApi)
    implementation(libs.feign.core)
    compileOnly(libs.javax.servlet.api)
    testImplementation(libs.test.junit4)
    testImplementation(libs.test.mockito.all)
    testImplementation(libs.javax.servlet.api)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}