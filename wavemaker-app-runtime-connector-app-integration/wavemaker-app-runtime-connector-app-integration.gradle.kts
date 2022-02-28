plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime.connector"

dependencies {
    implementation(enforcedPlatform(appDependencies.boms.springFramework.get()))
    implementation(projects.wavemakerAppRuntimeConnectorApi)
    implementation(projects.wavemakerCommonsUtil)
    implementation(appDependencies.slf4j.api)
    implementation(appDependencies.spring.beans)
    implementation(appDependencies.spring.context)
    implementation(appDependencies.snakeyaml)
    compileOnly(appDependencies.javax.servlet.api)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}