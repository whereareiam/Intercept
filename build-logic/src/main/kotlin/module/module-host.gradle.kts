// A module built against a host that runs on Java 25: Paper 26.3 and Velocity 4.2.0, the first releases on Adventure 5.
plugins {
    id("module-java")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
