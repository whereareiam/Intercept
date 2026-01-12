package me.whereareiam.intercept.common;

import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.dependency.DependencyLoader;
import me.whereareiam.intercept.model.dependency.LibraryDescriptor;
import me.whereareiam.intercept.model.dependency.RelocationRule;

import java.util.List;
import java.util.Objects;

public final class Dependencies {
	private static final List<String> BASE_REPOSITORIES = List.of(
			"https://repo1.maven.org/maven2/",
			"https://maven.whereareiam.me/development"
	);

	private static final List<LibraryDescriptor> BASE_LIBRARIES = List.of(
			LibraryDescriptor.builder()
					.groupId("com{}google{}inject")
					.artifactId("guice")
					.version(Constants.Dependency.GUICE)
					.relocation(RelocationRule.builder()
							.from("com{}google{}inject")
							.to("me.whereareiam.intercept.library.guice")
							.build())
					.relocation(RelocationRule.builder()
							.from("com{}google{}common")
							.to("me.whereareiam.intercept.library.guava")
							.build())
					.resolveTransitive(true)
					.build(),
			LibraryDescriptor.builder()
					.groupId("me.whereareiam")
					.artifactId("configura")
					.version(Constants.Dependency.CONFIGURA)
					.relocation(RelocationRule.builder()
							.from("com.fasterxml.jackson")
							.to("me.whereareiam.intercept.library.jackson")
							.build())
					.relocation(RelocationRule.builder()
							.from("org.yaml.snakeyaml")
							.to("me.whereareiam.intercept.library.snakeyaml")
							.build())
					.resolveTransitive(true)
					.build(),
			LibraryDescriptor.builder()
					.groupId("me.whereareiam")
					.artifactId("keystone")
					.version(Constants.Dependency.KEYSTONE)
					.resolveTransitive(true)
					.build(),
			LibraryDescriptor.builder()
					.groupId("me.whereareiam")
					.artifactId("commandant")
					.version(Constants.Dependency.COMMANDANT)
					.resolveTransitive(true)
					.build(),
			LibraryDescriptor.builder()
					.groupId("me.whereareiam")
					.artifactId("semantica")
					.version(Constants.Dependency.SEMANTICA)
					.resolveTransitive(true)
					.build(),
			LibraryDescriptor.builder()
					.groupId("me.whereareiam")
					.artifactId("dialectica")
					.version(Constants.Dependency.DIALECTICA)
					.resolveTransitive(true)
					.build(),
			LibraryDescriptor.builder()
					.groupId("org.incendo")
					.artifactId("cloud-core")
					.version(Constants.Dependency.CLOUD_CORE)
					.build(),
			LibraryDescriptor.builder()
					.groupId("org.incendo")
					.artifactId("cloud-processors-cooldown")
					.version(Constants.Dependency.CLOUD_COOLDOWN)
					.build(),
			LibraryDescriptor.builder()
					.groupId("org.incendo")
					.artifactId("cloud-annotations")
					.version(Constants.Dependency.CLOUD_CORE)
					.build(),
			LibraryDescriptor.builder()
					.groupId("org.incendo")
					.artifactId("cloud-minecraft-extras")
					.version(Constants.Dependency.CLOUD_MINECRAFT_EXTRAS)
					.build(),
			LibraryDescriptor.builder()
					.groupId("org.jdbi")
					.artifactId("jdbi3-core")
					.version(Constants.Dependency.JDBI)
					.build(),
			LibraryDescriptor.builder()
					.groupId("org.jdbi")
					.artifactId("jdbi3-sqlobject")
					.version(Constants.Dependency.JDBI)
					.build(),
			LibraryDescriptor.builder()
					.groupId("com.zaxxer")
					.artifactId("HikariCP")
					.version(Constants.Dependency.HIKARICP)
					.build(),
			LibraryDescriptor.builder()
					.groupId("org.postgresql")
					.artifactId("postgresql")
					.version(Constants.Dependency.POSTGRESQL)
					.build(),
			LibraryDescriptor.builder()
					.groupId("org.mariadb.jdbc")
					.artifactId("mariadb-java-client")
					.version(Constants.Dependency.MARIADB)
					.build()
	);

	private static final List<String> repositories = new java.util.ArrayList<>(BASE_REPOSITORIES);
	private static final List<LibraryDescriptor> libraries = new java.util.ArrayList<>(BASE_LIBRARIES);

	public static void addRepository(String repository) {
		repositories.add(Objects.requireNonNull(repository, "repository"));
	}

	public static void addLibrary(LibraryDescriptor library) {
		libraries.add(Objects.requireNonNull(library, "library"));
	}

	public static void applyTo(DependencyLoader loader) {
		Objects.requireNonNull(loader, "loader");
		repositories.forEach(loader::addRepository);
		libraries.forEach(loader::addLibrary);
	}
}
