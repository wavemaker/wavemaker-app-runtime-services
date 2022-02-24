plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(projects.wavemakerCommonsUtil)
    implementation(libs.commons.lang3)
    implementation(libs.javax.jaxws.api)
    api(libs.javax.jws.api) {
        because("soap runtime built on java 8 depends on javax.jws.soap package which is not available in java 11. " +
                "It's declared as api as it is needed in project compilation having soap services")
    }
    runtimeOnly(libs.javax.jaxws.rt) {
        because("soap runtime built on java 8 depends on javax.jws.soap package which is not available in java 11")
    }
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}