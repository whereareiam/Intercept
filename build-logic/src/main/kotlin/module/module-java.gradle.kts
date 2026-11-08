plugins {
    `java-library`
    id("io.freefair.lombok")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

group = "me.whereareiam.intercept"

// Compilation and tests use Java 21 whichever Java runs Gradle; NeoForge on Minecraft 1.21.1 runs on it.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    add("compileOnly", libs.findLibrary("guice").get())
    add("compileOnly", libs.findLibrary("annotations").get())
    add("compileOnly", libs.findLibrary("configura").get())
    add("compileOnly", libs.findLibrary("configura-feature-comment").get())
    add("compileOnly", libs.findBundle("adventure").get())

    add("testRuntimeOnly", libs.findLibrary("junit-platform").get())
    add("testImplementation", libs.findLibrary("junit-jupiter").get())
    add("testImplementation", libs.findLibrary("guice").get())
    add("testImplementation", libs.findLibrary("annotations").get())
    add("testImplementation", libs.findLibrary("configura").get())
    add("testImplementation", libs.findLibrary("configura-feature-comment").get())
    add("testImplementation", libs.findBundle("adventure").get())
    add("testImplementation", libs.findLibrary("mockito-core").get())
    add("testImplementation", libs.findLibrary("mockito-junit").get())

    if (path != ":intercept-api") {
        add("compileOnly", project(":intercept-api"))
        add("testImplementation", project(":intercept-api"))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// Tests tagged "integration" start servers in containers, so they run in integrationTest instead of test.
tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("integration")
    }
}

// Runs the container tests, and the "database" tests on PostgreSQL as well as on H2 and SQLite.
val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs the tests that start database and Redis servers in containers."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("integration", "database")
    }
    systemProperty("intercept.test.databaseServers", "true")
    // Most modules have no such test.
    failOnNoDiscoveredTests = false
    shouldRunAfter(tasks.named("test"))
}

tasks.named("check") {
    dependsOn(integrationTest)
}
