rootProject.name = "Intercept"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://registry.whereareiam.me/maven/packages")
        maven("https://maven.neoforged.net/releases")
    }
}

plugins {
    id("net.neoforged.moddev.repositories") version "2.0.148"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://registry.whereareiam.me/maven/packages")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-releases/")
        maven("https://repo.extendedclip.com/releases/")
        maven("https://maven.neoforged.net/releases")
    }
}

fun module(path: String, directory: String) {
    include(path)
    project(path).projectDir = file(directory)
}

module(":intercept-api", "intercept-api")
module(":intercept-common", "intercept-common")

module(":intercept-interception", "intercept-interception/interception")
module(":intercept-interception-api", "intercept-interception/interception-api")
module(":intercept-interception-common", "intercept-interception/interception-common")
module(":interception-library-common", "intercept-interception/interception-library/common")
module(":library-packetevents", "intercept-interception/interception-library/library-packetevents")
module(":library-mixin", "intercept-interception/interception-library/library-mixin")

module(":intercept-adapter-command", "intercept-adapter-command")

module(":intercept-adapter-persistence", "intercept-adapter-persistence/persistence")
module(":intercept-adapter-persistence-api", "intercept-adapter-persistence/persistence-api")
module(":intercept-adapter-persistence-common", "intercept-adapter-persistence/persistence-common")

module(":intercept-adapter-replication", "intercept-adapter-replication/replication")
module(":intercept-adapter-replication-api", "intercept-adapter-replication/replication-api")
module(":intercept-adapter-replication-common", "intercept-adapter-replication/replication-common")

module(":integration-placeholderapi", "intercept-integration/integration-placeholderapi")

module(":intercept-platform:common", "intercept-platform/common")
module(":intercept-platform:bundle", "intercept-platform/bundle")
module(":platform-paper", "intercept-platform/type/platform-paper")
module(":platform-velocity", "intercept-platform/type/platform-velocity")
module(":platform-neoforge", "intercept-platform/type/platform-neoforge")

module(":intercept-testing", "intercept-testing")
