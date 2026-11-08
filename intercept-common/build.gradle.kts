import me.whereareiam.attache.plugin.gradle.extension.AttacheExtension

plugins {
    id("module-java")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(libs.bundles.semantica)

    attache(libs.guice)
    attache(libs.configura)
    attache(libs.configura.feature.comment)
    attache(libs.semantica.api)
    attache(libs.semantica.common)
    attache(libs.semantica.syntax.icu)
    attache(libs.semantica.source.file)
    attache(libs.semantica.source.http)
    attache(libs.semantica.format.adventure.v5)

    testImplementation(libs.bundles.semantica)
}

// Intercept's classes are compiled against the relocated names; Attache relocates the downloaded jars to match.
extensions.configure<AttacheExtension>("attache") {
    library(libs.guice) {
        relocate("com{}google{}inject", "me.whereareiam.intercept.library.guice")
        relocate("com{}google{}common", "me.whereareiam.intercept.library.guava")
    }

    library(libs.configura) {
        relocate("com{}fasterxml{}jackson", "me.whereareiam.intercept.library.jackson")
        relocate("org{}yaml{}snakeyaml", "me.whereareiam.intercept.library.snakeyaml")
    }

    library(libs.configura.feature.comment) {
        relocate("com{}fasterxml{}jackson", "me.whereareiam.intercept.library.jackson")
        relocate("org{}yaml{}snakeyaml", "me.whereareiam.intercept.library.snakeyaml")
    }

    library(libs.semantica.api) {
        relocate("com{}fasterxml{}jackson", "me.whereareiam.intercept.library.jackson")
        relocate("org{}yaml{}snakeyaml", "me.whereareiam.intercept.library.snakeyaml")
    }

    library(libs.semantica.common) {
        relocate("com{}fasterxml{}jackson", "me.whereareiam.intercept.library.jackson")
        relocate("org{}yaml{}snakeyaml", "me.whereareiam.intercept.library.snakeyaml")
    }

    library(libs.semantica.syntax.icu) {
        relocate("com{}fasterxml{}jackson", "me.whereareiam.intercept.library.jackson")
        relocate("org{}yaml{}snakeyaml", "me.whereareiam.intercept.library.snakeyaml")
    }

    library(libs.semantica.source.file) {
        relocate("com{}fasterxml{}jackson", "me.whereareiam.intercept.library.jackson")
        relocate("org{}yaml{}snakeyaml", "me.whereareiam.intercept.library.snakeyaml")
    }

    library(libs.semantica.source.http) {
        relocate("com{}fasterxml{}jackson", "me.whereareiam.intercept.library.jackson")
        relocate("org{}yaml{}snakeyaml", "me.whereareiam.intercept.library.snakeyaml")
    }

    library(libs.semantica.format.adventure.v5) {
        relocate("com{}fasterxml{}jackson", "me.whereareiam.intercept.library.jackson")
        relocate("org{}yaml{}snakeyaml", "me.whereareiam.intercept.library.snakeyaml")
    }
}
