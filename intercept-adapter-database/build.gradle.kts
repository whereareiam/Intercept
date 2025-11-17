dependencies {
    "compileOnly"(project(":intercept-api"))
    "testImplementation"(project(":intercept-api"))

    // Ebean ORM
    "compileOnly"(libs.ebean.core)
    "compileOnly"(libs.ebean.api)

    // Connection pooling
    "compileOnly"(libs.hikaricp)

    // Database drivers
    "compileOnly"(libs.postgresql)
    "compileOnly"(libs.mariadb)
}