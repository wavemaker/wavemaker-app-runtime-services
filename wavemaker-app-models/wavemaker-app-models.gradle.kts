plugins {
    `java-library-maven-publish`
}
group = "com.wavemaker.app"

dependencies {
    implementation(enforcedPlatform(appDependenciesLibs.boms.jackson.get().toString()))
    implementation(enforcedPlatform(appDependenciesLibs.boms.springFramework.get().toString()))
    implementation(appDependenciesLibs.commons.lang3)
    implementation(appDependenciesLibs.jackson.annotations)
    implementation(appDependenciesLibs.jakarta.validationApi)
    implementation(appDependenciesLibs.spring.beans)
    implementation(appDependenciesLibs.spring.boot)
    implementation(appDependenciesLibs.swagger.core)
    implementation(projects.wavemakerCommonsUtil)
}