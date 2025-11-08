defaultTasks("shadowJar")

allprojects {
    version = (System.getenv("VERSION") ?: "dev")

    apply(plugin = "java")

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
    }

    dependencies {
        // lombok
        "compileOnly"(rootProject.libs.lombok)
        "annotationProcessor"(rootProject.libs.lombok)

        // general
        "compileOnly"(rootProject.libs.bundles.adventure)
        "compileOnly"(rootProject.libs.attache.common)
        "compileOnly"(rootProject.libs.configura)
        "compileOnly"(rootProject.libs.guice)
    }
}