defaultTasks("shadowJar")

allprojects {
    version = (System.getenv("VERSION") ?: "dev")
    group = "me.whereareiam"

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
}

subprojects {
    repositories {
        // mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.whereareiam.me/development")
        maven("https://maven.whereareiam.me/release")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    dependencies {
        // lombok
        "compileOnly"(rootProject.libs.lombok)
        "annotationProcessor"(rootProject.libs.lombok)

        // general
        "compileOnly"(rootProject.libs.guice)
        "compileOnly"(rootProject.libs.annotations)
        "compileOnly"(rootProject.libs.configura)
        "compileOnly"(rootProject.libs.commandant)
        "compileOnly"(rootProject.libs.keystone)
        "compileOnly"(rootProject.libs.semantica)
        "compileOnly"(rootProject.libs.dialectica)
        "compileOnly"(rootProject.libs.bundles.adventure)
        "implementation"(rootProject.libs.attache.common)

        // test
        "testImplementation"(rootProject.libs.guice)
        "testImplementation"(rootProject.libs.annotations)
        "testImplementation"(rootProject.libs.configura)
        "testImplementation"(rootProject.libs.commandant)
        "testImplementation"(rootProject.libs.keystone)
        "testImplementation"(rootProject.libs.semantica)
        "testImplementation"(rootProject.libs.dialectica)
        "testImplementation"(rootProject.libs.bundles.adventure)
        "testImplementation"(rootProject.libs.bundles.testing)
        "testRuntimeOnly"(rootProject.libs.junit.platform)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                val realm = (System.getenv("PUBLISH_REALM")
                    ?: if ((System.getenv("VERSION") ?: "dev").contains("dev", true)) "development" else "release")
                    .lowercase()
                url = uri("https://maven.whereareiam.me/$realm")
                credentials {
                    username = System.getenv("PUBLISH_USER") ?: ""
                    password = System.getenv("PUBLISH_TOKEN") ?: ""
                }
            }
        }
    }
}
