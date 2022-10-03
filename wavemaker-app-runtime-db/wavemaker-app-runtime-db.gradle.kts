plugins {
    `java-library-maven-publish`
    `antlr`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(enforcedPlatform(appDependenciesLibs.boms.springFramework.get()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.springData.get()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.jackson.get()))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimeCommons)
    implementation(appDependenciesLibs.slf4j.api)
    implementation(appDependenciesLibs.commons.lang3)
    implementation(appDependenciesLibs.guava)
    implementation(appDependenciesLibs.spring.beans)
    implementation(appDependenciesLibs.spring.context)
    implementation(appDependenciesLibs.spring.orm)
    implementation(appDependenciesLibs.spring.web)
    implementation(appDependenciesLibs.spring.data.commons)
    implementation(appDependenciesLibs.jackson.annotations)
    implementation(appDependenciesLibs.hibernate.core)
    implementation(appDependenciesLibs.aspectjrt)
    implementation(appDependenciesLibs.aspectjweaver)
    implementation(appDependenciesLibs.jakarta.validationApi)
    implementation(appDependenciesLibs.jakarta.xml.bindapi)
    implementation(appDependenciesLibs.javax.annotation.api)
    implementation(appDependenciesLibs.poiOoxml) {
        exclude("com.github.virtuald", "curvesapi")
    }
    implementation(appDependenciesLibs.freemarker)
    implementation(appDependenciesLibs.tika.core)
    compileOnly(appDependenciesLibs.javax.servlet.api)
    runtimeOnly(appDependenciesLibs.antlr4Runtime)
    // The below dependency is adding antlr4 and its transitive dependencies as well, removing them using exclude for runtime configuration
    antlr(appDependenciesLibs.build.antlr4)
    runtimeOnly(appDependenciesLibs.antlr4Runtime)
    runtimeOnly(appDependenciesLibs.hikariCP)
    testImplementation(appDependenciesLibs.test.junit4)
    testImplementation(appDependenciesLibs.test.testng)
    testImplementation(appDependenciesLibs.commons.text)

    //TODO: To support DB2 implementation, the custom code needs to be updated as per the new hibernate library code
    /*runtimeLibDependencies(appDependenciesLibs.hibernate.core) {
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