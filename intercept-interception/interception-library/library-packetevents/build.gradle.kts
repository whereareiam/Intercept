plugins {
    id("module-java")
}

dependencies {
    compileOnly(projects.interceptInterceptionApi)
    compileOnly(projects.interceptionLibraryCommon)
    compileOnly(libs.packetevents.api)
    compileOnly(libs.semantica.format.adventure.common)
}
