plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(platform(projects.wavemakerAppRuntimeServices))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimePrefab)
    implementation(libs.commons.io)
    implementation(libs.commons.lang3)
    implementation(libs.spring.context)
    implementation(libs.spring.web)
    implementation(libs.spring.webmvc)
    implementation(libs.spring.data.commons)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.datatype.hibernate5)
    compileOnly(libs.javax.servlet.api)
    testImplementation(libs.test.junit4)
    testImplementation(libs.test.jsonassert)
    testRuntimeOnly(libs.hibernate.core) {
        because("jackson-datatype-hibernate5 depends on hibernate-core, TODO need to remove dependency on " +
                "jackson-datatype-hibernate5 also in this module")
    }
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}