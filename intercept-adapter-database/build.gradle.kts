dependencies {
    "compileOnly"(project(":intercept-api"))
    "compileOnly"(rootProject.libs.attache.common)

    // Ebean ORM
    "compileOnly"(libs.ebean.core)
    "compileOnly"(libs.ebean.api)

    // Database drivers
    "compileOnly"(libs.postgresql)
    "compileOnly"(libs.mariadb)
}