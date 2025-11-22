package me.whereareiam.intercept.adapter.database;

import lombok.RequiredArgsConstructor;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.attache.model.Library;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.DependencyResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Dependency resolver for database-related libraries.
 * Conditionally loads Jdbi and database drivers based on database configuration.
 */
@RequiredArgsConstructor
public class DatabaseDependencyResolver implements DependencyResolver {
	private final LibraryManager libraryManager;
	private final List<Library> libraries = new ArrayList<>();

	@Override
	public void resolveDependencies() {
		libraryManager.addMavenCentral();
		libraries.forEach(libraryManager::loadLibrary);
		clearDependencies();
	}

	@Override
	public void loadLibraries() {
		// Jdbi core runtime
		addDependency(Library.builder()
				.groupId("org{}jdbi")
				.artifactId("jdbi3-core")
				.version(Constants.Dependency.JDBI)
				.resolveTransitiveDependencies(true)
				.build());

		// Jdbi SQL Object support
		addDependency(Library.builder()
				.groupId("org{}jdbi")
				.artifactId("jdbi3-sqlobject")
				.version(Constants.Dependency.JDBI)
				.resolveTransitiveDependencies(true)
				.build());

		// HikariCP connection pool
		addDependency(Library.builder()
				.groupId("com{}zaxxer")
				.artifactId("HikariCP")
				.version(Constants.Dependency.HIKARICP)
				.resolveTransitiveDependencies(true)
				.build());

		// Database drivers - only load the selected type
		// Jdbi talks to whichever JDBC driver we provide
		switch (Constants.Database.TYPE) {
			case POSTGRES:
				// PostgreSQL JDBC driver
				addDependency(Library.builder()
						.groupId("org{}postgresql")
						.artifactId("postgresql")
						.version(Constants.Dependency.POSTGRESQL)
						.resolveTransitiveDependencies(true)
						.build());
				break;
			case MARIADB:
				// MariaDB JDBC driver
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

