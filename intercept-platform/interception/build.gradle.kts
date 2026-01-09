import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.withType

subprojects {
    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-releases/")
    }

    dependencies {
        val interceptionCommonPath = ":intercept-platform:interception:common"

        if (project.path.contains(":interception:") && project.path != interceptionCommonPath) {
            "implementation"(project(interceptionCommonPath))
        }
    }

    tasks.withType<ShadowJar> {
        relocate("me.whereareiam.attache", "me.whereareiam.intercept.library.attache")

        relocate("com.google.common", "me.whereareiam.intercept.library.guava")
        relocate("com.google.inject", "me.whereareiam.intercept.library.guice")
    }
}
