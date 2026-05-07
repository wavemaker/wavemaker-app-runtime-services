plugins {
    `catalog-maven-publish`
}

group = "ai.wavemaker.app"

catalogMavenPublish {
    scmUrl = "git:https://github.com/wavemaker/wavemaker-app-runtime-services.git"
    catalogName = "appDependenciesLibs"
}