package me.whereareiam.intercept.common;

import me.whereareiam.attache.LibraryManager;
import me.whereareiam.attache.model.Library;
import me.whereareiam.attache.model.Relocation;
import me.whereareiam.attache.type.VerbosityMode;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.DependencyResolver;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonDependencyResolver implements DependencyResolver {
	protected LibraryManager libraryManager;
	protected final List<Library> libraries = new ArrayList<>();

	@Override
	public void resolveDependencies() {
		libraryManager.setVerbosityMode(VerbosityMode.QUIET);

		libraryManager.addMavenCentral();
		libraryManager.addRepository("https://maven.whereareiam.me/development");
	}

	@Override
	public void loadLibraries() {
		// Common libraries
		addDependency(Library.builder()
				.groupId("com{}google{}inject")
				.artifactId("guice")
				.version(Constants.Dependency.GUICE)
				.resolveTransitiveDependencies(true)
				.relocations(List.of(
								Relocation.builder()
										.pattern("com{}google{}inject")
										.relocatedPattern("me.whereareiam.intercept.library.guice")
										.build(),
								Relocation.builder()
										.pattern("com{}google{}common")
										.relocatedPattern("me.whereareiam.intercept.library.guava")
										.build()
						)
				)
				.build());

		addDependency(Library.builder()
				.groupId("me.whereareiam")
				.artifactId("configura")
				.version(Constants.Dependency.CONFIGURA)
				.resolveTransitiveDependencies(true)
				.relocation(
						Relocation.builder()
								.pattern("com{}fasterxml{}jackson")
								.relocatedPattern("me.whereareiam.intercept.library.jackson")
								.build()
				).relocation(
						Relocation.builder()
								.pattern("org{}yaml{}snakeyaml")
								.relocatedPattern("me.whereareiam.intercept.library.snakeyaml")
								.build()
				).build());
	}

	@Override
	public void addDependency(Library library) {
		libraries.add(library);
	}

	@Override
	public void clearDependencies() {
		libraries.clear();
	}
}

