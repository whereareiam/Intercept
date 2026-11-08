import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("packaging-platform-jar")
    id("net.neoforged.moddev")
}

description = "Intercept on NeoForge: a server-side mod for Minecraft 1.21.1"

platform {
    name.set("NEOFORGE")
    // A mod jar of its own; the bundle holds the plugins.
    bundled.set(false)
}

neoForge {
    version = libs.versions.neoforge.get()
}

/**
 * NeoForge has no Attache, so the mod carries its libraries. Its private ones are shaded and relocated below
 * me.whereareiam.intercept.library, out of every other mod's way. Adventure and semantica's core and Adventure format
 * are nested jars instead (NeoForge's Jar-in-Jar): every mod that ships them shares one copy, so they share Adventure's
 * global translator and semantica's TextArgument, which Intercept's authority depends on.
 */
val nested = listOf(
    libs.adventure.asProvider(),
    libs.adventure.key,
    libs.adventure.minimessage,
    libs.adventure.plain,
    libs.adventure.gson,
    libs.adventure.json,
    libs.adventure.commons,
    libs.option,
    libs.jspecify,
    // Adventure's module requires JetBrains' annotations at run time.
    libs.annotations,
    libs.semantica.api,
    libs.semantica.common,
    libs.semantica.syntax.icu,
    libs.semantica.format.common,
    libs.semantica.format.adventure.common,
    libs.semantica.format.adventure.v5,
    libs.icu4j,
    // Cloud for NeoForge is a mod of its own, with Cloud's core nested in it.
    libs.cloud.neoforge,
)

// The game brings these and pins them to its own versions; the libraries below ask for newer ones.
val gameLibraries = mapOf(
    "org.slf4j:slf4j-api" to "2.0.9",
    "com.google.code.gson:gson" to "2.10.1",
)

configurations.configureEach {
    resolutionStrategy.eachDependency {
        gameLibraries["${requested.group}:${requested.name}"]?.let { useVersion(it) }
    }
}

/**
 * What Shadow puts into the mod jar: Intercept's modules and its private libraries, without the game and NeoForge,
 * which the compile classpath also holds.
 */
val shade: Configuration by configurations.creating {
    exclude(group = "net.kyori")
    exclude(group = "org.jspecify")
    exclude(group = "org.jetbrains")
    exclude(group = "com.ibm.icu")
    // Cloud for NeoForge carries its core and geantyref.
    exclude(group = "org.incendo")
    exclude(group = "io.leangen.geantyref")
    exclude(group = "com.google.code.gson")
    exclude(group = "org.slf4j")
    // Annotations only, which the game's libraries bring as well.
    exclude(group = "org.checkerframework")
    exclude(group = "com.google.errorprone")
    exclude(group = "com.google.j2objc")
    exclude(group = "com.google.code.findbugs")
    exclude(group = "me.whereareiam.semantica", module = "semantica-api")
    exclude(group = "me.whereareiam.semantica", module = "semantica-common")
    exclude(group = "me.whereareiam.semantica", module = "syntax-icu")
    exclude(group = "me.whereareiam.semantica", module = "format-common")
    exclude(group = "me.whereareiam.semantica", module = "format-adventure-common")
    exclude(group = "me.whereareiam.semantica", module = "format-adventure-v5")
}

dependencies {
    implementation(projects.libraryMixin)
    implementation(projects.interceptionLibraryCommon)

    implementation(libs.guice)
    implementation(libs.configura)
    implementation(libs.configura.feature.comment)
    implementation(libs.semantica.source.file)
    implementation(libs.semantica.source.http)
    implementation(libs.commandant)
    implementation(libs.dialectica.bundle)
    implementation(libs.dialectica.adapter.jdbi)
    implementation(libs.strata)
    implementation(libs.strata.adapter.database)
    implementation(libs.strata.adapter.database.jdbi)
    implementation(libs.strata.adapter.database.dialectica)
    implementation(libs.jdbi.core)
    implementation(libs.hikaricp)
    implementation(libs.postgresql)
    implementation(libs.mariadb)
    implementation(libs.sqlite)
    implementation(libs.h2)
    implementation(libs.jedis)

    compileOnly(projects.interceptInterceptionApi)
    compileOnly(projects.interceptAdapterPersistenceApi)
    compileOnly(libs.cloud.core)
    compileOnly(libs.cloud.neoforge) { isTransitive = false }
    compileOnly(libs.adventure.gson) { exclude(group = "com.google.code.gson") }
    compileOnly(libs.semantica.format.adventure.common)

    shade(projects.interceptApi)
    shade(projects.interceptCommon)
    shade(projects.interceptPlatform.common)
    shade(projects.interceptInterception)
    shade(projects.interceptAdapterCommand)
    shade(projects.interceptAdapterPersistence)
    shade(projects.interceptAdapterReplication)
    shade(projects.libraryMixin)
    shade(projects.interceptionLibraryCommon)
    shade(libs.guice)
    shade(libs.configura)
    shade(libs.configura.feature.comment)
    shade(libs.semantica.source.file)
    shade(libs.semantica.source.http)
    shade(libs.commandant)
    shade(libs.dialectica.bundle)
    shade(libs.dialectica.adapter.jdbi)
    shade(libs.strata)
    shade(libs.strata.adapter.database)
    shade(libs.strata.adapter.database.jdbi)
    shade(libs.strata.adapter.database.dialectica)
    shade(libs.jdbi.core)
    shade(libs.hikaricp)
    shade(libs.postgresql)
    shade(libs.mariadb)
    shade(libs.sqlite)
    shade(libs.h2)
    shade(libs.jedis)

    for (library in nested) jarJar(library) { isTransitive = false }
}

// NeoForge reads a mod's version as a number; a development build such as "dev" or "dev-1a2b3c4" becomes 0.0.0-dev.
val modVersion = provider {
    val version = project.version.toString()
    if (version.first().isDigit()) version else "0.0.0-$version"
}

tasks.processResources {
    val version = modVersion
    inputs.property("version", version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to version.get())
    }
}

tasks.named<ShadowJar>("shadowJar") {
    from(tasks.named("jarJar"))

    configurations = listOf(shade)

    relocate("me.whereareiam.configura", "me.whereareiam.intercept.library.configura")
    relocate("me.whereareiam.semantica.source.file", "me.whereareiam.intercept.library.semantica.source.file")
    relocate("me.whereareiam.semantica.source.http", "me.whereareiam.intercept.library.semantica.source.http")
    relocate("me.whereareiam.commandant", "me.whereareiam.intercept.library.commandant")
    relocate("me.whereareiam.keystone", "me.whereareiam.intercept.library.keystone")
    relocate("org.json", "me.whereareiam.intercept.library.json")
    relocate("me.whereareiam.dialectica", "me.whereareiam.intercept.library.dialectica")
    relocate("me.whereareiam.strata", "me.whereareiam.intercept.library.strata")
    relocate("com.zaxxer.hikari", "me.whereareiam.intercept.library.hikari")
    relocate("redis.clients", "me.whereareiam.intercept.library.jedis")
    relocate("org.apache.commons.pool2", "me.whereareiam.intercept.library.pool2")
    relocate("org.aopalliance", "me.whereareiam.intercept.library.aopalliance")
    relocate("com.google.thirdparty", "me.whereareiam.intercept.library.guava.thirdparty")
    relocate("jakarta.inject", "me.whereareiam.intercept.library.inject")

    exclude("META-INF/maven/**", "META-INF/versions/*/module-info.class", "module-info.class", "META-INF/*.kotlin_module")
}

tasks.named<Jar>("jar") { archiveClassifier.set("plain") }
