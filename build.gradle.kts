defaultTasks("shadowJar")

allprojects {
    version = (System.getenv("VERSION") ?: "dev")

    apply(plugin = "java-library")

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
        "compileOnly"(rootProject.libs.bundles.adventure)
        "compileOnly"(rootProject.libs.attache.common)
        "compileOnly"(rootProject.libs.configura)
        "compileOnly"(rootProject.libs.commandant)
        "compileOnly"(rootProject.libs.keystone)
        "compileOnly"(rootProject.libs.dialectica)
        "compileOnly"(rootProject.libs.guice)

        // test
        "testImplementation"(rootProject.libs.configura)
        "testImplementation"(rootProject.libs.commandant)
        "testImplementation"(rootProject.libs.keystone)
        "testImplementation"(rootProject.libs.guice)
        "testImplementation"(rootProject.libs.bundles.adventure)
        "testImplementation"(rootProject.libs.bundles.testing)
        "testRuntimeOnly"(rootProject.libs.junit.platform)
    }
}