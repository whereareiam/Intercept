tasks.named<Jar>("jar").configure { enabled = false }
tasks.named("shadowJar").configure { enabled = false }

subprojects {
    if (!project.path.endsWith(":common")) {
        dependencies {
            "implementation"(project(":intercept-platform:interception:platform-bukkit:common"))
            "implementation"(project(":intercept-integration:integration-placeholderapi"))
            "compileOnly"(project(":intercept-api"))
        }
    }

    repositories {
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}
