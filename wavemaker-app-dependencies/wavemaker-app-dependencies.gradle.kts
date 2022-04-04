plugins {
    `catalog-maven-publish`
}

group = "com.wavemaker.app"

catalogMavenPublish {
    scmUrl = "git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
    catalogName = "appDependenciesLibs"
}