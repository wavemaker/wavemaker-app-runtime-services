plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime"

dependencies {
    implementation(platform(project(":wavemaker-app-runtime-services")))
    implementation(project(":wavemaker-commons-util"))
    implementation("org.slf4j:slf4j-api")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.apache.commons:commons-collections4")
    implementation("org.springframework:spring-webmvc")
    compileOnly("javax.servlet:javax.servlet-api")
    testImplementation("junit:junit")
    testImplementation("org.springframework:spring-test")
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}