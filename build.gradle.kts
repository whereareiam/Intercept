import me.whereareiam.attache.plugin.gradle.extension.AttacheExtension
import me.whereareiam.toolkit.versioning.extension.ToolkitVersioningExtension

plugins {
    alias(libs.plugins.attache)
    alias(libs.plugins.toolkit.distribution)
    alias(libs.plugins.toolkit.versioning)
}

allprojects {
    version = rootProject.extensions
        .getByType(ToolkitVersioningExtension::class.java)
        .resolvedVersion()
        .get()
}

extensions.configure<AttacheExtension>("attache") {
    transitive.set(true)

    mavenLocal()
    repository("https://registry.whereareiam.me/maven/packages")
}

defaultTasks("pluginJars")

tasks.register("pluginJars") {
    group = "build"
    description = "Builds the Paper and Velocity plugin jars, the bundle of both and the NeoForge mod."

    dependsOn(
        ":platform-paper:shadowJar",
        ":platform-velocity:shadowJar",
        ":platform-neoforge:shadowJar",
        ":intercept-platform:bundle:shadowJar"
    )
}

// Files a development build or a release ships; assembleDistribution writes them to build/distribution.
toolkitDistribution {
    file("BUNDLE", ":intercept-platform:bundle", "shadowJar")
    file("PAPER", ":platform-paper", "shadowJar")
    file("VELOCITY", ":platform-velocity", "shadowJar")
    file("NEOFORGE", ":platform-neoforge", "shadowJar")
}
