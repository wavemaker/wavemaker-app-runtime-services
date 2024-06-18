plugins {
    `java-library-maven-publish`
}
group = "com.wavemaker.app"

dependencies {
    compileOnly(enforcedPlatform(appDependenciesLibs.boms.springFramework.get().toString()))
    compileOnly(appDependenciesLibs.spring.core)
    compileOnly(appDependenciesLibs.slf4j.api)
    compileOnly(appDependenciesLibs.jakarta.servlet.api)
}