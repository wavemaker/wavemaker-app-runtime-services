plugins {
    `java-platform-maven-publish`
}

javaPlatform {
    allowDependencies()
}

group = "com.wavemaker.app"

javaPlatformMavenPublish {
    scmUrl = "git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
    addCatalogLibraryDependency("slf4j.api")
    addCatalogLibraryDependency("commons.codec")
    addCatalogLibraryDependency("commons.collections4")
    addCatalogLibraryDependency("commons.compress")
    addCatalogLibraryDependency("commons.configuration2")
    addCatalogLibraryDependency("commons.io")
    addCatalogLibraryDependency("commons.fileupload")
    addCatalogLibraryDependency("commons.lang3")
    addCatalogLibraryDependency("commons.text")
    addCatalogLibraryDependency("guava")
    addCatalogLibraryDependency("gson")
    addCatalogLibraryDependency("hibernate.core")
    addCatalogLibraryDependency("hibernate.validator")
    addCatalogLibraryDependency("apache.httpclient")
    addCatalogLibraryDependency("hikariCP")
    addCatalogLibraryDependency("jdbc.driver.hsqldb")
    addCatalogLibraryDependency("jdbc.driver.mysql")
    addCatalogLibraryDependency("jdbc.driver.postgresql")
    addCatalogLibraryDependency("jdbc.driver.redshift")
    addCatalogLibraryDependency("jdbc.driver.ngdbc")

    addCatalogLibraryBomDependency("springFramework")
    addCatalogLibraryBomDependency("springSecurity")
    addCatalogLibraryBomDependency("springSession")
    addCatalogLibraryBomDependency("springData")
    addCatalogLibraryBomDependency("jackson")
    addCatalogLibraryBomDependency("log4j")
}