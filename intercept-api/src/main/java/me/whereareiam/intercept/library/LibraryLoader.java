package me.whereareiam.intercept.library;

import org.jetbrains.annotations.NotNull;

/**
 * Loads a group of runtime libraries, such as the driver of one database, before it is used. Platforms that ship
 * every library inside the jar load nothing.
 */
public interface LibraryLoader {
	/**
	 * Loads the libraries of the given groups.
	 *
	 * @param groups group names as declared in the build, such as {@code postgres}
	 */
	void load(@NotNull String... groups);
}
