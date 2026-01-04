dependencies {
	"api"(project(":intercept-api"))
	"api"(project(":intercept-platform:interception:common"))
	"compileOnly"(libs.bundles.database)

	// Test dependencies
	"testImplementation"(project(":intercept-api"))
	"testImplementation"(project(":intercept-platform:interception:common"))
	"testImplementation"(libs.testcontainers.mariadb)
    "testImplementation"(libs.testcontainers.postgres)
    "testImplementation"(libs.bundles.database)
    "testImplementation"(libs.bundles.testing)
}

tasks.test {
    useJUnitPlatform()
}
