plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(platform(projects.wavemakerAppRuntimeServices))
    implementation(projects.wavemakerCommonsUtil)
    implementation(libs.commons.lang3)
    implementation(libs.javax.jaxws.api)
    runtimeOnly(libs.javax.jws.api) {
        because("soap runtime built on java 8 depends on javax.jws.soap package which is not available in java 11")
    }
    runtimeOnly(libs.javax.jaxws.rt) {
        because("soap runtime built on java 8 depends on javax.jws.soap package which is not available in java 11")
    }
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}