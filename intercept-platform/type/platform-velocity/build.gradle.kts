plugins {
    id("module-host")
    id("packaging-platform-jar")
    alias(libs.plugins.attache)
}

platform {
    name.set("VELOCITY")
    descriptors.add("velocity-plugin.json")
}

dependencies {
    implementation(projects.libraryPacketevents)
    implementation(projects.interceptionLibraryCommon)
    implementation(libs.attache.common)
    implementation(libs.attache.velocity)

    compileOnly(projects.interceptInterceptionApi)
    compileOnly(libs.velocity)
    compileOnly(libs.packetevents.api)
    compileOnly(libs.cloud.core)
    compileOnly(libs.cloud.velocity)

    attache(libs.cloud.velocity)
}

tasks.processResources {
    val properties = mapOf("version" to project.version.toString())
    inputs.properties(properties)
    filesMatching("velocity-plugin.json") {
        expand(properties)
    }
}
