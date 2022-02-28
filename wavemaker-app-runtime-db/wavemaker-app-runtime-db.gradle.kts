plugins {
    `java-library-maven-publish`
    `antlr`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(enforcedPlatform(appDependencies.boms.springFramework.get()))
    implementation(enforcedPlatform(appDependencies.boms.springData.get()))
    implementation(enforcedPlatform(appDependencies.boms.jackson.get()))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimeCommons)
    implementation(appDependencies.slf4j.api)
    implementation(appDependencies.commons.lang3)
    implementation(appDependencies.guava)
    implementation(appDependencies.spring.beans)
    implementation(appDependencies.spring.context)
    implementation(appDependencies.spring.orm)
    implementation(appDependencies.spring.web)
    implementation(appDependencies.spring.data.commons)
    implementation(appDependencies.jackson.annotations)
    implementation(appDependencies.hibernate.core)
    implementation(appDependencies.aspectjrt)
    implementation(appDependencies.aspectjweaver)
    implementation(appDependencies.javax.annotation.api)
    implementation(appDependencies.javax.validationApi)
    implementation(appDependencies.poiOoxml) {
        exclude("com.github.virtuald", "curvesapi")
    }
    implementation(appDependencies.freemarker)
    implementation(appDependencies.tika.core)
    compileOnly(appDependencies.javax.servlet.api)
    runtimeOnly(appDependencies.antlr4Runtime)
    // The below dependency is adding antlr4 and its transitive dependencies as well, removing them using exclude for runtime configuration
    antlr(appDependencies.build.antlr4)
    runtimeOnly(appDependencies.antlr4Runtime)
    runtimeOnly(appDependencies.hikariCP)
    testImplementation(appDependencies.test.junit4)
    testImplementation(appDependencies.test.testng)
    testImplementation(appDependencies.commons.text)

    //TODO: To support DB2 implementation, the custom code needs to be updated as per the new hibernate library code
    /*runtimeLibDependencies(appDependencies.hibernate.core) {
        version {
            strictly("5.2.17.WM")
        }
    }*/
}

configurations {
    runtimeOnly {
        exclude("org.antlr", "antlr4")
    }
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}

tasks {
    withType<Jar>().configureEach { // This is added to make sourcesJar task depend on generateGrammarSource task
        dependsOn(generateGrammarSource)
    }
}