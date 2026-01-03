import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadow)
}

subprojects {
    plugins.apply(rootProject.libs.plugins.shadow.get().pluginId)

    tasks.withType<ShadowJar> {
        archiveBaseName.set(rootProject.name)

        relocate("me.whereareiam.attache", "me.whereareiam.intercept.library.attache")

        relocate("com.google.common", "me.whereareiam.intercept.library.guava")
        relocate("com.google.inject", "me.whereareiam.intercept.library.guice")

        val defaultDestination = rootProject.layout.buildDirectory.dir("libs")

        val customOutputDir = if (project.hasProperty("output")) {
            project.layout.dir(project.provider { File(project.property("output").toString()) })
        } else {
            null
        }

        if (!project.path.endsWith(":common"))
            destinationDirectory.set(customOutputDir ?: defaultDestination)
    }

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-releases/")
    }

    dependencies {
        val interceptionCommonPath = ":intercept-platform:interception:common"

        "implementation"(rootProject.libs.attache.common)
        "implementation"(project(":intercept-common"))

        if (project.path.contains(":interception:") && project.path != interceptionCommonPath) {
            "implementation"(project(interceptionCommonPath))
        }

        if (project.path != interceptionCommonPath) {
            "implementation"(project(":intercept-adapter-command"))
            "implementation"(project(":intercept-adapter-database"))
        }
    }

    tasks.named<Jar>("jar") {
        dependsOn("shadowJar")
    }
}
