dependencies {
	"compileOnly"(project(":intercept-api"))
	"testImplementation"(project(":intercept-api"))
}

tasks.test {
    useJUnitPlatform()
}
