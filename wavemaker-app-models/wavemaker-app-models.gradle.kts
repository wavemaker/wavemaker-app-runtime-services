plugins {
    `java-library-maven-publish`
}
group = "com.wavemaker.app"

dependencies {
    implementation(libs.jakarta.validationApi)
    implementation(libs.commons.lang3)
}