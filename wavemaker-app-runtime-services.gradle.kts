plugins {
    `java-platform-maven-publish`
}

javaPlatform {
    allowDependencies()
}

group ="com.wavemaker.runtime"

dependencies {
    api(enforcedPlatform(libs.boms.springFramework.get()))
    api(enforcedPlatform(libs.boms.springSecurity.get()))
    api(enforcedPlatform(libs.boms.springSession.get()))
    api(enforcedPlatform(libs.boms.springData.get()))
    api(enforcedPlatform(libs.boms.jackson.get()))
    api(enforcedPlatform(libs.boms.log4j.get()))
    constraints {
        api(libs.slf4j.api)
        api(libs.commons.codec)
        api(libs.commons.collections4)
        api(libs.commons.compress)
        api(libs.commons.configuration2)
        api(libs.commons.io)
        api(libs.commons.fileupload)
        api(libs.commons.lang3)
        api(libs.commons.text)
        api(libs.guava)
        api(libs.gson)
        api(libs.spring.security.saml2.service.provider)
        api(libs.hibernate.core)
        api(libs.hibernate.validator)
        api(libs.apache.httpclient)
        api(libs.hikariCP)
        api(libs.javax.servlet.api)
    }
}

javaPlatformMavenPublish {
    scmUrl="git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
}
