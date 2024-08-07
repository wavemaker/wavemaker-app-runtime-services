plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(projects.wavemakerCommonsUtil)
    implementation(appDependenciesLibs.commons.lang3)
    implementation(appDependenciesLibs.jakarta.xml.ws.api)
    api(appDependenciesLibs.jakarta.jws.api) {
        because("soap runtime built on java 8 depends on javax.jws.soap package which is not available in java 11. " +
                "It's declared as api as it is needed in project compilation having soap services")
    }
    runtimeOnly(appDependenciesLibs.javax.jaxws.rt) {
        because("soap runtime built on java 8 depends on javax.jws.soap package which is not available in java 11")
    }
    compileOnly(appDependenciesLibs.jaxb.impl)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}