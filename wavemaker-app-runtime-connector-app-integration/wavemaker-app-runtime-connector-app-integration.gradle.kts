plugins {
    `java-library-maven-publish`
}

group ="com.wavemaker.runtime.connector"

dependencies {
    implementation(platform(project(":wavemaker-app-runtime-services")))
    implementation(project(":wavemaker-app-runtime-connector-api"))
    implementation(project(":wavemaker-commons-util"))
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework:spring-beans")
    implementation("org.springframework:spring-context")
    implementation("org.yaml:snakeyaml:1.26")
    compileOnly("javax.servlet:javax.servlet-api")
}

javaLibraryMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}