plugins {
    id("module-java")
    id("net.neoforged.moddev")
}

neoForge {
    version = libs.versions.neoforge.get()
}

dependencies {
    compileOnly(projects.interceptInterceptionApi)
    compileOnly(projects.interceptionLibraryCommon)
    // The game brings its own Gson.
    compileOnly(libs.adventure.gson) { exclude(group = "com.google.code.gson") }
    compileOnly(libs.semantica.format.adventure.common)
}
