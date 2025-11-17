package me.whereareiam.intercept.adapter.database;

import lombok.RequiredArgsConstructor;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.attache.model.Library;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.DependencyResolver;
import me.whereareiam.intercept.model.config.Database;
import me.whereareiam.intercept.type.DatabaseType;

import java.util.ArrayList;
import java.util.List;

/**
 * Dependency resolver for database-related libraries.
 * Conditionally loads Ebean and database drivers based on database configuration.
 */
@RequiredArgsConstructor
public class DatabaseDependencyResolver implements DependencyResolver {
	private final LibraryManager libraryManager;
	private final Database databaseConfig;
	private final List<Library> libraries = new ArrayList<>();

	@Override
	public void resolveDependencies() {
		if (!databaseConfig.isEnabled())
			return;

		libraryManager.addMavenCentral();
		libraries.forEach(libraryManager::loadLibrary);
		clearDependencies();
	}

	@Override
	public void loadLibraries() {
		if (!databaseConfig.isEnabled())
			return;

		// Ebean ORM dependencies
		addDependency(Library.builder()
				.groupId("io{}ebean")
				.artifactId("ebean-core")
				.version(Constants.Dependency.EBEAN)
				.resolveTransitiveDependencies(true)
				.build());

		addDependency(Library.builder()
				.groupId("io{}ebean")
				.artifactId("ebean-api")
				.version(Constants.Dependency.EBEAN)
				.resolveTransitiveDependencies(true)
				.build());

		// Database driver - only load the selected type
		DatabaseType dbType = databaseConfig.getType() != null ? databaseConfig.getType() : DatabaseType.POSTGRES;
		switch (dbType) {
			case POSTGRES:
				addDependency(Library.builder()
						.groupId("org{}postgresql")
						.artifactId("postgresql")
						.version(Constants.Dependency.POSTGRESQL)
						.resolveTransitiveDependencies(true)
						.build());
				break;
			case MARIADB:
				addDependency(Library.builder()
						.groupId("org{}mariadb{}jdbc")
						.artifactId("mariadb-java-client")
						.version(Constants.Dependency.MARIADB)
						.resolveTransitiveDependencies(true)
						.build());
				break;
		}
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

