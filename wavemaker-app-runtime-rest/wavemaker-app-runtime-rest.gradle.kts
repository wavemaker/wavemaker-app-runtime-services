plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(enforcedPlatform(appDependencies.boms.springFramework.get()))
    implementation(enforcedPlatform(appDependencies.boms.jackson.get()))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimeCommons)
    implementation(projects.wavemakerToolsApidocsCore)
    implementation(appDependencies.commons.collections4)
    implementation(appDependencies.commons.io)
    implementation(appDependencies.commons.lang3)
    implementation(appDependencies.commons.text)
    implementation(appDependencies.spring.context)
    implementation(appDependencies.spring.web)
    implementation(appDependencies.spring.webmvc)
    implementation(appDependencies.jackson.annotations)
    implementation(appDependencies.apache.httpclient)
    implementation(appDependencies.javax.annotation.api)
    implementation(appDependencies.javax.validationApi)
    implementation(appDependencies.feign.core)
    compileOnly(appDependencies.javax.servlet.api)
    testImplementation(appDependencies.test.junit4)
    testImplementation(appDependencies.test.mockito.all)
    testImplementation(appDependencies.javax.servlet.api)
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}