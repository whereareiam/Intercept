subprojects {
    dependencies {
        val directCommonPath = ":intercept-platform:direct:common"

        if (project.path.contains(":direct:") && project.path != directCommonPath) {
            "implementation"(project(directCommonPath))
        }
    }
}