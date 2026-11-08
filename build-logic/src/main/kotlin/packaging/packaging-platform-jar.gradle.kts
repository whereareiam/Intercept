import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import me.whereareiam.intercept.buildlogic.bundle.bundlePlatformId
import me.whereareiam.intercept.buildlogic.bundle.platform
import me.whereareiam.intercept.buildlogic.bundle.platformCollection

plugins {
    id("packaging-shaded-jar")
}

val platformCollection = platformCollection()
val platform = platform()

dependencies {
    add("implementation", project(":intercept-api"))
    add("implementation", project(":intercept-common"))
    add("implementation", project(":intercept-platform:common"))
    add("implementation", project(":intercept-interception"))
    add("implementation", project(":intercept-adapter-command"))
    add("implementation", project(":intercept-adapter-persistence"))
    add("implementation", project(":intercept-adapter-replication"))
}

afterEvaluate {
    tasks.named<ShadowJar>("shadowJar").configure {
        archiveClassifier.set(bundlePlatformId())
    }

    if (platform.bundled.get())
        platformCollection.memberPaths.add(project.path)
}
