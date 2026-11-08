import java.security.MessageDigest

plugins {
    java
    alias(libs.plugins.anvil)
    alias(libs.plugins.anvil.junit)
    alias(libs.plugins.anvil.capability.default)
    alias(libs.plugins.anvil.platform.paper)
    alias(libs.plugins.anvil.platform.velocity)
    alias(libs.plugins.anvil.platform.neoforge)
}

description = "Live journeys: the packaged plugin and mod on real Paper, Velocity and NeoForge servers"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

/**
 * The probe: a plugin the Paper journeys install beside Intercept, which owns a Semantica and registers its own
 * translator, as the network's other plugins do. It carries Semantica inside its jar.
 */
val probe: SourceSet by sourceSets.creating

dependencies {
    "probeCompileOnly"(libs.paper)
    "probeImplementation"(libs.semantica.common)
    "probeImplementation"(libs.semantica.syntax.icu)
    "probeImplementation"(libs.semantica.format.adventure.v5) { exclude(group = "net.kyori") }

    anvilCompileOnly(libs.lombok)
    anvilRuntimeOnly(libs.anvil.protocol.mcprotocol)
    // The database and Redis run in Docker containers beside the servers.
    anvilRuntimeOnly(libs.docker.java.core)
    anvilRuntimeOnly(libs.docker.java.transport)
    anvilAnnotationProcessor(libs.lombok)
}

/**
 * The output of a project, built before the journeys that install it.
 */
fun output(path: String, configuration: String): Configuration =
    configurations.detachedConfiguration(dependencies.project(path, configuration)).apply { isTransitive = false }

/**
 * A plugin release the journeys install as an administrator would, downloaded once and checked against its hash.
 */
fun release(name: String, url: String, sha512: String): FileCollection {
    val target = layout.buildDirectory.file("releases/$name.jar")
    val download = tasks.register("download${name.replaceFirstChar(Char::uppercase)}") {
        outputs.file(target)
        doLast {
            val file = target.get().asFile
            file.parentFile.mkdirs()
            uri(url).toURL().openStream().use { input -> file.outputStream().use(input::copyTo) }

            val digest = MessageDigest.getInstance("SHA-512")
                .digest(file.readBytes())
                .joinToString("") { "%02x".format(it) }
            if (digest != sha512) throw GradleException("$url has SHA-512 $digest, expected $sha512")
        }
    }

    return files(download)
}

val packeteventsPaper = release(
    "packeteventsPaper",
    "https://cdn.modrinth.com/data/HYKaKraK/versions/m78nFxYg/packetevents-spigot-2.14.0.jar",
    "18a942cb55f64783a63b28deea2cebbbe782e5d6a1089f6700430e183bfbce0a45398a640ed0dff4361b55190dcf47017f2ca7788f4a5c8c9efad1f549a63fe3"
)
val packeteventsVelocity = release(
    "packeteventsVelocity",
    "https://cdn.modrinth.com/data/HYKaKraK/versions/p0asH9aC/packetevents-velocity-2.14.0.jar",
    "0a09d5d11907646cbc21f96a1db43f60b27fe5a582a69e425953e7c2905d37829dcc0ff4a48a274b949ca6692200e2586fd6003f80b80b018c404927eebb55b6"
)

// The probe compiles against Paper, which runs on Java 25.
tasks.named<JavaCompile>("compileProbeJava") {
    javaCompiler.set(javaToolchains.compilerFor { languageVersion.set(JavaLanguageVersion.of(25)) })
}

val probeJar = tasks.register<Jar>("probeJar") {
    archiveBaseName.set("probe")
    from(probe.output)
    from(probe.runtimeClasspath.filter { it.name.endsWith(".jar") }.elements.map { jars -> jars.map { zipTree(it) } })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class", "META-INF/versions/*/module-info.class")
}

// One suite per platform: anvilPaperTest, anvilVelocityTest and anvilNeoforgeTest run the journeys of that tag.
val journeySuites = listOf("paper", "velocity", "neoforge")

anvil {
    acceptEula()
    engine {
        protocolLibrary("mcprotocol")
        // A server sizes its thread pools from the processors it sees; all of them at once stall the machine.
        processors.set(4)
        if (!System.getProperty("os.name").lowercase().contains("win")) processPriority.set("low")
    }

    artifact("intercept-paper", output(":platform-paper", "shadowRuntimeElements"))
    artifact("intercept-velocity", output(":platform-velocity", "shadowRuntimeElements"))
    artifact("intercept-neoforge", output(":platform-neoforge", "shadowRuntimeElements"))
    artifact("packetevents-paper", packeteventsPaper)
    artifact("packetevents-velocity", packeteventsVelocity)
    artifact("probe", probeJar)

    suites {
        journeySuites.forEach { suite ->
            register(suite) {
                includeTags(suite)
            }
        }
    }
}
