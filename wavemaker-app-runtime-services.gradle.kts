buildscript {
    extra.apply {
        set("commonsIoVersion", "2.8.0")
        set("commonsFileUploadVersion", "1.4")
        set("commonsCollections4Version", "4.4")
        set("commonsLang3Version", "3.11")
        set("commonsTextVersion", "1.9")
        set("slf4jVersion", "1.7.30")
        set("guavaVersion", "30.0-jre")
        set("springVersion", "5.3.5")
        set("springSecurityVersion", "5.4.5")
        set("springSessionVersion", "Dragonfruit-SR1")
        set("springSecuritySamlVersion", "1.0.3.RELEASE")
        set("springDataCommonsVersion", "2.3.4.RELEASE")
        set("hibernateVersion", "5.4.22.Final")
        set("hibernateValidatorVersion", "6.1.6.Final")
        set("httpClientVersion", "4.5.13")
        set("hikariCPVersion","3.4.5")
        set("jacksonVersion", "2.11.3")
        set("mockitoVersion", "1.10.19")
        set("junitVersion", "4.13.1")
        set("testngVersion", "7.3.0")
        set("servletVersion", "3.1.0")
        set("log4j2Version", "2.13.3")
    }
}

plugins {
    `java-platform-maven-publish`
}

javaPlatform {
    allowDependencies()
}

group ="com.wavemaker.runtime"

dependencies {
    api(enforcedPlatform("org.springframework:spring-framework-bom:${project.extra["springVersion"]}"))
    api(enforcedPlatform("org.springframework.security:spring-security-bom:${project.extra["springSecurityVersion"]}"))
    api(enforcedPlatform("org.springframework.session:spring-session-bom:${project.extra["springSessionVersion"]}"))
    api(enforcedPlatform("com.fasterxml.jackson:jackson-bom:${project.extra["jacksonVersion"]}"))
    api(enforcedPlatform("org.apache.logging.log4j:log4j-bom:${project.extra["log4j2Version"]}"))
    constraints {
        api("org.slf4j:slf4j-api:${project.extra["slf4jVersion"]}")
        api("org.apache.commons:commons-collections4:${project.extra["commonsCollections4Version"]}")
        api("commons-io:commons-io:${project.extra["commonsIoVersion"]}")
        api("commons-fileupload:commons-fileupload:${project.extra["commonsFileUploadVersion"]}")
        api("org.apache.commons:commons-lang3:${project.extra["commonsLang3Version"]}")
        api("org.apache.commons:commons-text:${project.extra["commonsTextVersion"]}")
        api("com.google.guava:guava:${project.extra["guavaVersion"]}")
        api("org.springframework.data:spring-data-jpa:${project.extra["springDataCommonsVersion"]}")
        api("org.springframework.security.extensions:spring-security-saml2-core:${project.extra["springSecuritySamlVersion"]}")
        api("org.hibernate:hibernate-core:${project.extra["hibernateVersion"]}")
        api("org.hibernate:hibernate-validator:${project.extra["hibernateValidatorVersion"]}")
        api("org.apache.httpcomponents:httpclient:${project.extra["httpClientVersion"]}")
        api("com.zaxxer:HikariCP:${project.extra["hikariCPVersion"]}")
        api("javax.servlet:javax.servlet-api:${project.extra["servletVersion"]}")
        api("junit:junit:${project.extra["junitVersion"]}")
        api("org.testng:testng:${project.extra["testngVersion"]}")
        api("org.mockito:mockito-all:${project.extra["mockitoVersion"]}")
    }
}

javaPlatformMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}
