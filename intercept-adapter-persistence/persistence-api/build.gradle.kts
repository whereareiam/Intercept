plugins {
    id("module-api")
}

dependencies {
    compileOnlyApi(libs.dialectica)
    compileOnlyApi(libs.dialectica.adapter.jdbi)
    compileOnlyApi(libs.jdbi.core)
    compileOnlyApi(libs.strata)
    compileOnlyApi(libs.strata.adapter.database)
}

toolkitPublish {
    artifactId.set("persistence-api")
}
