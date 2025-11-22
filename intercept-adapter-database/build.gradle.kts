dependencies {
    "compileOnly"(project(":intercept-api"))

    "compileOnly"(libs.jdbi.core)
    "compileOnly"(libs.jdbi.sqlobject)

    // Connection pooling
    "compileOnly"(libs.hikaricp)

    // Database drivers
    "compileOnly"(libs.postgresql)
    "compileOnly"(libs.mariadb)
}