rootProject.name = "Intercept"

include("intercept-integration:integration-placeholderapi")
include("intercept-platform:interception:platform-bukkit:common")
include("intercept-platform:interception:platform-bukkit:bukkit")
include("intercept-platform:interception:platform-bukkit:paper")
include("intercept-platform:interception:platform-velocity")
include("intercept-platform:interception:platform-bukkit")
include("intercept-adapter-database")
include("intercept-adapter-command")
include("intercept-api")
include("intercept-common")
include("intercept-platform:interception:common")
include("intercept-platform:direct:common")

val withOraylen = providers.gradleProperty("withOraylen").orNull?.toBoolean() ?: false
val oraylenRepo = File(System.getProperty("user.home"), ".m2/repository/net/oraylen/oraylen/dev")
val oraylenArtifact = oraylenRepo.resolve("oraylen-dev.pom").isFile
		|| oraylenRepo.resolve("oraylen-dev.jar").isFile

if (oraylenArtifact) {
	include("intercept-platform:direct:platform-oraylen")
} else if (withOraylen) {
	println("Skipping intercept-platform:direct:platform-oraylen because net.oraylen:oraylen:dev is missing from mavenLocal.")
}
