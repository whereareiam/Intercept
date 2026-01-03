dependencies {
    "api"(project(":intercept-api"))
    "compileOnly"(libs.bundles.database)

    // Test dependencies
    "testImplementation"(project(":intercept-api"))
    "testImplementation"(libs.testcontainers.mariadb)
    "testImplementation"(libs.testcontainers.postgres)
    "testImplementation"(libs.bundles.database)
    "testImplementation"(libs.bundles.testing)
}

tasks.test {
    useJUnitPlatform()
}
