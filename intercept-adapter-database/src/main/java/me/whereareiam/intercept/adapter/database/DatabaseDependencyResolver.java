package me.whereareiam.intercept.adapter.database;

import lombok.RequiredArgsConstructor;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.attache.model.Library;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.DependencyResolver;
import me.whereareiam.intercept.model.config.Persistence;

import java.util.ArrayList;
import java.util.List;

/**
 * Dependency resolver for database-related libraries.
 * Conditionally loads OrmLite and database drivers based on database configuration.
 */
@RequiredArgsConstructor
public class DatabaseDependencyResolver implements DependencyResolver {
	private final LibraryManager libraryManager;
	private final Persistence persistence;
	private final List<Library> libraries = new ArrayList<>();

	@Override
	public void resolveDependencies() {
		if (!persistence.isEnabled())
			return;

		libraryManager.addMavenCentral();
		libraries.forEach(libraryManager::loadLibrary);
		clearDependencies();
	}

	@Override
	public void loadLibraries() {
		if (!persistence.isEnabled())
			return;

		// OrmLite ORM dependencies
		// OrmLite core library
		addDependency(Library.builder()
				.groupId("com{}j256{}ormlite")
				.artifactId("ormlite-core")
				.version(Constants.Dependency.ORMLITE)
				.resolveTransitiveDependencies(true)
				.build());

		// OrmLite JDBC library
		addDependency(Library.builder()
				.groupId("com{}j256{}ormlite")
				.artifactId("ormlite-jdbc")
				.version(Constants.Dependency.ORMLITE)
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
		// OrmLite supports PostgreSQL and MariaDB/MySQL natively, no platform provider needed
		switch (persistence.getType()) {
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

