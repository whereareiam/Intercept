import org.apache.tools.ant.filters.ReplaceTokens

tasks.named<Jar>("jar").configure { enabled = false }
tasks.named("shadowJar").configure { enabled = false }

subprojects {
    if (!project.path.endsWith(":common")) {
        dependencies {
            "implementation"(project(":intercept-platform:interception:platform-bukkit:common"))
            "implementation"(project(":intercept-integration:integration-placeholderapi"))
        }

        tasks.named<Copy>("processResources") {
            filter<ReplaceTokens>(
                "tokens" to mapOf(
                    "projectName" to rootProject.name,
                    "projectVersion" to project.version
                )
            )
        }
    }

    repositories {
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}
