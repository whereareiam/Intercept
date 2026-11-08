import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow")
}

tasks.withType<ShadowJar>().configureEach {
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    mergeServiceFiles()

    relocate("com.google.inject", "me.whereareiam.intercept.library.guice")
    relocate("com.google.common", "me.whereareiam.intercept.library.guava")
    relocate("org.jdbi", "me.whereareiam.intercept.library.jdbi")
    relocate("com.fasterxml.jackson", "me.whereareiam.intercept.library.jackson")
    relocate("org.yaml.snakeyaml", "me.whereareiam.intercept.library.snakeyaml")
}
