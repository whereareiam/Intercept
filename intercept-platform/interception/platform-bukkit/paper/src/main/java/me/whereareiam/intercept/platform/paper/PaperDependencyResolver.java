package me.whereareiam.intercept.platform.paper;

import me.whereareiam.attache.LibraryManager;
import me.whereareiam.attache.model.Library;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.common.CommonDependencyResolver;

public class PaperDependencyResolver extends CommonDependencyResolver {
	public PaperDependencyResolver(LibraryManager libraryManager) {
		this.libraryManager = libraryManager;
	}

	@Override
	public void resolveDependencies() {
		super.resolveDependencies();
		libraryManager.addRepository("https://repo.codemc.io/repository/maven-releases/");

		libraries.forEach(libraryManager::loadLibrary);
		clearDependencies();
	}

	@Override
	public void loadLibraries() {
		super.loadLibraries();

		// Paper specific libraries
		addDependency(Library.builder()
				.groupId("net{}kyori")
				.artifactId("adventure-api")
				.version(Constants.Dependency.ADVENTURE)
				.build());

		addDependency(Library.builder()
				.groupId("net{}kyori")
				.artifactId("adventure-text-minimessage")
				.version(Constants.Dependency.ADVENTURE)
				.build());

		addDependency(Library.builder()
				.groupId("net{}kyori")
				.artifactId("adventure-text-serializer-legacy")
				.version(Constants.Dependency.ADVENTURE)
				.build());

		addDependency(Library.builder()
				.groupId("net{}kyori")
				.artifactId("adventure-text-serializer-plain")
				.version(Constants.Dependency.ADVENTURE)
				.build());

		addDependency(Library.builder()
				.groupId("net{}kyori")
				.artifactId("adventure-text-serializer-gson")
				.version(Constants.Dependency.ADVENTURE)
				.build());

		addDependency(Library.builder()
				.groupId("net{}kyori")
				.artifactId("adventure-platform-bukkit")
				.version(Constants.Dependency.ADVENTURE_PLATFORM_BUKKIT)
				.build());

		addDependency(Library.builder()
				.groupId("org{}incendo")
				.artifactId("cloud-paper")
				.version(Constants.Dependency.CLOUD_PAPER)
				.resolveTransitiveDependencies(true)
				.build());
	}
}