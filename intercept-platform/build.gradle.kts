import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    alias(libs.plugins.shadow)
}

subprojects {
    plugins.apply(rootProject.libs.plugins.shadow.get().pluginId)

    tasks.withType<ShadowJar> {
        archiveBaseName.set(rootProject.name)

        val defaultDestination = rootProject.layout.buildDirectory.dir("libs")

        val customOutputDir = if (project.hasProperty("output")) {
            project.layout.dir(project.provider { File(project.property("output").toString()) })
        } else {
            null
        }

        if (!project.path.endsWith(":common"))
            destinationDirectory.set(customOutputDir ?: defaultDestination)
    }

    // Configure resource token replacement for platform-specific modules
    if (project.path.contains(":platform-") && !project.path.endsWith(":common")) {
        tasks.named<Copy>("processResources") {
            filter<ReplaceTokens>(
                "tokens" to mapOf(
                    "projectName" to rootProject.name,
                    "projectVersion" to project.version
                )
            )
        }
    }

    dependencies {
        "implementation"(project(":intercept-common"))
        "implementation"(project(":intercept-adapter-command"))
        "implementation"(project(":intercept-adapter-database"))

        "implementation"(rootProject.libs.attache.common)
    }

    tasks.named<Jar>("jar") {
        dependsOn("shadowJar")
    }
}
