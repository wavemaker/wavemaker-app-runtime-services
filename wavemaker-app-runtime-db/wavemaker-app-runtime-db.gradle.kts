plugins {
    `java-library-maven-publish`
    `antlr`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(platform(projects.wavemakerAppRuntimeServices))
    implementation(projects.wavemakerCommonsUtil)
    implementation(projects.wavemakerAppRuntimeCommons)
    implementation(libs.slf4j.api)
    implementation(libs.commons.lang3)
    implementation(libs.guava)
    implementation(libs.spring.beans)
    implementation(libs.spring.context)
    implementation(libs.spring.orm)
    implementation(libs.spring.web)
    implementation(libs.spring.data.commons)
    implementation(libs.jackson.annotations)
    implementation(libs.hibernate.core)
    implementation(libs.aspectjrt)
    implementation(libs.aspectjweaver)
    implementation(libs.javax.annotation.api)
    implementation(libs.javax.validationApi)
    implementation(libs.poi)
    implementation(libs.poiOoxml) {
        exclude("com.github.virtuald", "curvesapi")
    }
    implementation(libs.freemarker)
    implementation(libs.tika.core)
    compileOnly(libs.javax.servlet.api)
    runtimeOnly(libs.antlr4Runtime)
    // The below dependency is adding antlr4 and its transitive dependencies as well, removing them using exclude for runtime configuration
    antlr(libs.antlr4)
    runtimeOnly(libs.antlr4Runtime)
    runtimeOnly(libs.hikariCP)
    testImplementation(libs.test.junit4)
    testImplementation(libs.test.testng)
    testImplementation(libs.commons.text)

    //TODO: To support DB2 implementation, the custom code needs to be updated as per the new hibernate library code
    /*runtimeLibDependencies(libs.hibernate.core) {
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