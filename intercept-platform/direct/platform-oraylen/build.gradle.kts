tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_25.toString()
    targetCompatibility = JavaVersion.VERSION_25.toString()
}

repositories {
    mavenLocal()
}

dependencies {
    "compileOnly"(libs.oraylen)
    "testImplementation"(libs.oraylen)
}
