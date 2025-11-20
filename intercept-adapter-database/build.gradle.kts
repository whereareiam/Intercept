dependencies {
    "compileOnly"(project(":intercept-api"))
    "testImplementation"(project(":intercept-api"))

    // OrmLite ORM - use implementation so classes can be accessed during build
    // OrmLite runtime libraries are still loaded via LibraryManager at runtime
    "implementation"(libs.ormlite.core)
    "implementation"(libs.ormlite.jdbc)

    // Connection pooling
    "compileOnly"(libs.hikaricp)

    // Database drivers
    "compileOnly"(libs.postgresql)
    "compileOnly"(libs.mariadb)
}