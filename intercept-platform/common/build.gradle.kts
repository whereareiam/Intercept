plugins {
    id("module-java")
}

dependencies {
    compileOnly(projects.interceptCommon)
    compileOnly(projects.interceptInterception)
    compileOnly(projects.interceptAdapterCommand)
    compileOnly(projects.interceptAdapterPersistence)
    compileOnly(projects.interceptAdapterReplication)
    compileOnly(libs.cloud.core)
    compileOnly(libs.attache.common)
    compileOnly(libs.slf4j)
}
