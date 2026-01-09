package me.whereareiam.intercept.platform.bukkit;

import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.common.CommonDependencyResolver;
import me.whereareiam.intercept.common.InterceptDependencyLoader;
import me.whereareiam.intercept.model.dependency.LibraryDescriptor;

import java.util.List;

public class PaperDependencyResolver extends CommonDependencyResolver {
	private static final List<LibraryDescriptor> PAPER_LIBRARIES = List.of(
			LibraryDescriptor.builder()
					.groupId("org.incendo")
					.artifactId("cloud-paper")
					.version(Constants.Dependency.CLOUD_PAPER)
					.resolveTransitive(true)
					.build()
	);

	public PaperDependencyResolver(LibraryManager libraryManager) {
		super(new InterceptDependencyLoader(libraryManager, true));
	}

	@Override
	protected void addRepositories() {
		super.addRepositories();
		dependencyLoader.addRepository("https://repo.codemc.io/repository/maven-releases/");
	}

	@Override
	public void loadLibraries() {
		super.loadLibraries();
		PAPER_LIBRARIES.forEach(this::addDependencySpec);
	}
}
