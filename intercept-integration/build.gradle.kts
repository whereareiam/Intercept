subprojects {
    dependencies {
        "compileOnly"(project(":intercept-api"))
        "compileOnly"(rootProject.libs.guice)
        "compileOnly"(rootProject.libs.keystone)
    }
}

