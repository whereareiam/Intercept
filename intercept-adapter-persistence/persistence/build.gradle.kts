plugins {
    id("module-java")
}

dependencies {
    implementation(projects.interceptAdapterPersistenceApi)
    implementation(projects.interceptAdapterPersistenceCommon)
}
