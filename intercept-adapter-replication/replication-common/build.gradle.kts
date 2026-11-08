import me.whereareiam.attache.plugin.gradle.extension.AttacheExtension

plugins {
    id("module-java")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(projects.interceptAdapterReplicationApi)
    compileOnly(libs.jedis)

    testImplementation(projects.interceptAdapterReplicationApi)
    testImplementation(libs.jedis)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.redis)
}

// Loaded only when replication is enabled.
extensions.configure<AttacheExtension>("attache") {
    group("redis") {
        library(libs.jedis)
    }
}
