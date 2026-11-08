plugins {
    id("module-java")
}

dependencies {
    compileOnly(projects.interceptInterceptionApi)

    testImplementation(projects.interceptInterceptionApi)
}
