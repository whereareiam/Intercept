plugins {
    id("module-java")
    alias(libs.plugins.attache)
}

dependencies {
    compileOnly(libs.bundles.cloud)
    compileOnly(libs.commandant)
    compileOnly(libs.semantica.format.adventure.v5)

    attache(libs.cloud.core)
    attache(libs.cloud.annotations)
    attache(libs.commandant)

    testImplementation(libs.bundles.cloud)
    testImplementation(libs.commandant)
    testImplementation(libs.bundles.semantica)
}
