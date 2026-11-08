plugins {
    id("module-java")
}

dependencies {
    implementation(projects.interceptAdapterReplicationApi)
    implementation(projects.interceptAdapterReplicationCommon)
}
