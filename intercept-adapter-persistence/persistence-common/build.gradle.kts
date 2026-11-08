import me.whereareiam.attache.plugin.gradle.extension.AttacheExtension

plugins {
    id("module-java")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(projects.interceptAdapterPersistenceApi)
    compileOnly(libs.dialectica.bundle)
    compileOnly(libs.dialectica.adapter.jdbi)
    compileOnly(libs.strata)
    compileOnly(libs.strata.adapter.database)
    compileOnly(libs.strata.adapter.database.jdbi)
    compileOnly(libs.strata.adapter.database.dialectica)
    compileOnly(libs.jdbi.core)
    compileOnly(libs.hikaricp)

    attache(libs.dialectica.bundle)
    attache(libs.dialectica.adapter.jdbi)
    attache(libs.strata)
    attache(libs.strata.adapter.database)
    attache(libs.strata.adapter.database.jdbi)
    attache(libs.strata.adapter.database.dialectica)
    attache(libs.jdbi.core)
    attache(libs.hikaricp)

    testImplementation(projects.interceptAdapterPersistenceApi)
    testImplementation(libs.dialectica.bundle)
    testImplementation(libs.dialectica.adapter.jdbi)
    testImplementation(libs.strata)
    testImplementation(libs.strata.adapter.database)
    testImplementation(libs.strata.adapter.database.jdbi)
    testImplementation(libs.strata.adapter.database.dialectica)
    testImplementation(libs.jdbi.core)
    testImplementation(libs.hikaricp)
    testImplementation(libs.h2)
    testImplementation(libs.sqlite)
    testImplementation(libs.postgresql)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
}

extensions.configure<AttacheExtension>("attache") {
    library(libs.dialectica.adapter.jdbi) {
        relocate("org{}jdbi", "me.whereareiam.intercept.library.jdbi")
        relocate("io{}leangen{}geantyref", "me.whereareiam.intercept.library.geantyref")
        // Dialectica comes from the bundle, in the version Intercept declares.
        excludeTransitive("me.whereareiam", "dialectica-api")
        excludeTransitive("me.whereareiam", "dialectica-common")
    }

    library(libs.strata.adapter.database.jdbi) {
        relocate("org{}jdbi", "me.whereareiam.intercept.library.jdbi")
        relocate("io{}leangen{}geantyref", "me.whereareiam.intercept.library.geantyref")
    }

    library(libs.strata.adapter.database.dialectica) {
        // Dialectica comes from the bundle, in the version Intercept declares.
        excludeTransitive("me.whereareiam", "dialectica")
        excludeTransitive("me.whereareiam", "dialectica-api")
        excludeTransitive("me.whereareiam", "dialectica-common")
    }

    library(libs.jdbi.core) {
        relocate("org{}jdbi", "me.whereareiam.intercept.library.jdbi")
        relocate("io{}leangen{}geantyref", "me.whereareiam.intercept.library.geantyref")
    }

    group("postgres") {
        library(libs.postgresql)
    }

    group("mysql") {
        library(libs.mariadb)
    }

    group("sqlite") {
        library(libs.sqlite)
    }

    group("h2") {
        library(libs.h2)
    }
}
