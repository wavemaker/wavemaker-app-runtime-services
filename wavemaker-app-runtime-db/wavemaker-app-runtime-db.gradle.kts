plugins {
    `java-library-maven-publish`
    `antlr`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(enforcedPlatform(appDependenciesLibs.boms.slf4j.get().toString()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.springFramework.get().toString()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.springData.get().toString()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.jackson.get().toString()))
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
    implementation(libs.jakarta.persistenceApi) {
        because("This is explicitly added even if the module is not directly depending on this dependency. hibernate-core depends on " +
                "javax.persistence-api but it is not available as it is declared as scope provided in parent_pom.xml." +
                "To get the transaction related classes adding jakarta.persistence-api")
    }
    implementation(libs.jakarta.transaction.api) {
        because("This is explicitly added even if the module is not directly depending on this dependency. hibernate-core depends on " +
                "javax.persistence-api but it is not available as it is declared as scope provided in parent_pom.xml." +
                "To get the transaction related classes adding jakarta.transaction-api")
    }
    implementation(appDependenciesLibs.aspectjrt)
    implementation(appDependenciesLibs.aspectjweaver)
    implementation(appDependenciesLibs.jakarta.annotation.api)
    implementation(appDependenciesLibs.jakarta.validationApi)
    implementation(appDependenciesLibs.poiOoxml) {
        exclude("com.github.virtuald", "curvesapi")
    }
    implementation(appDependenciesLibs.freemarker)
    implementation(appDependenciesLibs.tika.core)
    compileOnly(appDependenciesLibs.jakarta.servlet.api)
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