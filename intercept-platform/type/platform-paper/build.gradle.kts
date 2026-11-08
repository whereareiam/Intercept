plugins {
    id("module-host")
    id("packaging-platform-jar")
    alias(libs.plugins.attache)
}

platform {
    name.set("PAPER")
    descriptors.add("paper-plugin.yml")
}

dependencies {
    implementation(projects.libraryPacketevents)
    implementation(projects.interceptionLibraryCommon)
    implementation(projects.integrationPlaceholderapi)
    implementation(libs.attache.common)
    implementation(libs.attache.paper)

    compileOnly(projects.interceptInterceptionApi)
    compileOnly(projects.interceptAdapterPersistenceApi)
    compileOnly(projects.interceptAdapterReplicationApi)
    compileOnly(libs.paper)
    compileOnly(libs.packetevents.api)
    compileOnly(libs.cloud.core)
    compileOnly(libs.cloud.paper)

    attache(libs.cloud.paper)
}

tasks.processResources {
    val properties = mapOf("version" to project.version.toString())
    inputs.properties(properties)
    filesMatching("paper-plugin.yml") {
        expand(properties)
    }
}
