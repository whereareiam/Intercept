dependencies {
	"compileOnly"(project(":intercept-api"))
	"implementation"(project(":intercept-common"))
	"testImplementation"(project(":intercept-api"))
	"testImplementation"(project(":intercept-common"))
}

tasks.test {
    useJUnitPlatform()
}
