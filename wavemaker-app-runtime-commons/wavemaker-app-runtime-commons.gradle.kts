plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(enforcedPlatform(appDependenciesLibs.boms.springFramework.get()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.springData.get()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.jackson.get()))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimePrefab)
    implementation(appDependenciesLibs.commons.io)
    implementation(appDependenciesLibs.commons.lang3)
    implementation(appDependenciesLibs.spring.context)
    implementation(appDependenciesLibs.spring.web)
    implementation(appDependenciesLibs.spring.webmvc)
    implementation(appDependenciesLibs.spring.data.commons)
    implementation(appDependenciesLibs.jackson.dataformat.yaml)
    implementation(appDependenciesLibs.jackson.datatype.jsr310)
    implementation(appDependenciesLibs.jackson.datatype.hibernate5)
    compileOnly(appDependenciesLibs.javax.servlet.api)
    testImplementation(appDependenciesLibs.test.junit4)
    testImplementation(appDependenciesLibs.test.jsonassert)
    testRuntimeOnly(appDependenciesLibs.hibernate.core) {
        because("jackson-datatype-hibernate5 depends on hibernate-core, TODO need to remove dependency on " +
                "jackson-datatype-hibernate5 also in this module")
    }
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}