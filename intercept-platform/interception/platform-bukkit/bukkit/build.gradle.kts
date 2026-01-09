import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

tasks.withType<ShadowJar> {
    archiveClassifier.set("BUKKIT")

    manifest {
        attributes(
            "Plugin-Type" to "BUKKIT"
        )
    }
}

dependencies {
    "compileOnly"(libs.bundles.bukkit)
}