buildscript {
    extra.apply {
        set("wavemakerApiDocsToolsVersion", "2.24")
        set("commonsIoVersion", "2.6")
        set("commonsFileUploadVersion", "1.4")
        set("commonsCollections4Version", "4.2")
        set("commonsLang3Version", "3.9")
        set("commonsTextVersion", "1.6")
        set("slf4jVersion", "1.7.29")
        set("guavaVersion", "27.0.1-jre")
        set("springVersion", "5.1.4.RELEASE")
        set("springSecurityVersion", "5.0.8.RELEASE")
        set("springSessionVersion", "Dragonfruit-RELEASE")
        set("springSecuritySamlVersion", "1.0.3.RELEASE")
        set("springDataCommonsVersion", "2.0.10.RELEASE")
        set("hibernateVersion", "5.2.17.Final")
        set("hibernateValidatorVersion", "5.4.2.Final")
        set("httpClientVersion", "4.5.9")
        set("hikariCPVersion","3.4.1")
        set("jacksonVersion", "2.9.8")
        set("mockitoVersion", "1.10.19")
        set("junitVersion", "4.11")
        set("testngVersion", "6.14.3")
        set("servletVersion", "3.1.0")
    }
}

plugins {
    `java-platform`
    `maven-publish`
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
    constraints {
        api("com.wavemaker.tools.apidocs:wavemaker-tools-apidocs-core:${project.extra["wavemakerApiDocsToolsVersion"]}")
        api("com.wavemaker.tools.apidocs:wavemaker-tools-apidocs-parser:${project.extra["wavemakerApiDocsToolsVersion"]}")
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

publishing {
    configurePublicationToDist(this)
    publications {
        create<MavenPublication>("maven") {
            artifactId = project.extensions.extraProperties.get("basename") as String
            from(components["javaPlatform"])
        }
    }
}
