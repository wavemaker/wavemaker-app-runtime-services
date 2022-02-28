plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(enforcedPlatform(appDependencies.boms.springFramework.get()))
    implementation(enforcedPlatform(appDependencies.boms.springData.get()))
    implementation(enforcedPlatform(appDependencies.boms.jackson.get()))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimePrefab)
    implementation(appDependencies.commons.io)
    implementation(appDependencies.commons.lang3)
    implementation(appDependencies.spring.context)
    implementation(appDependencies.spring.web)
    implementation(appDependencies.spring.webmvc)
    implementation(appDependencies.spring.data.commons)
    implementation(appDependencies.jackson.dataformat.yaml)
    implementation(appDependencies.jackson.datatype.jsr310)
    implementation(appDependencies.jackson.datatype.hibernate5)
    compileOnly(appDependencies.javax.servlet.api)
    testImplementation(appDependencies.test.junit4)
    testImplementation(appDependencies.test.jsonassert)
    testRuntimeOnly(appDependencies.hibernate.core) {
        because("jackson-datatype-hibernate5 depends on hibernate-core, TODO need to remove dependency on " +
                "jackson-datatype-hibernate5 also in this module")
    }
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}