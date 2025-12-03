repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    "compileOnly"(rootProject.libs.spigot)
    "compileOnly"(rootProject.libs.placeholderAPI)
}