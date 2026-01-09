package me.whereareiam.intercept.dependency;

import me.whereareiam.intercept.model.dependency.LibraryDescriptor;

/**
 * Interface for managing and resolving runtime dependencies in the Intercept plugin.
 * Provides functionality to handle library dependencies using a platform-specific loader,
 * allowing dynamic loading and management of external dependencies.
 */
public interface DependencyResolver {
	/**
	 * Adds repositories and resolves registered dependencies via the platform loader.
	 */
	void resolveDependencies();

	/**
	 * Registers libraries that should be resolved and loaded.
	 */
	void loadLibraries();

	/**
	 * Adds a new dependency to be resolved and loaded.
	 *
	 * @param library the library dependency to add
	 */
	void addDependency(LibraryDescriptor library);
}


