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
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.whereareiam.me/development")
    }

    dependencies {
        // lombok
        "compileOnly"(rootProject.libs.lombok)
        "annotationProcessor"(rootProject.libs.lombok)

        // general
        "implementation"(rootProject.libs.bundles.adventure) // TODO Temporary
        "implementation"(rootProject.libs.guice) // TODO Temporary
        "implementation"(rootProject.libs.configura) // TODO Temporary
    }
}