package me.whereareiam.intercept.platform.library;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.library.LibraryLoader;
import org.jetbrains.annotations.NotNull;

/**
 * Loads library groups through Attache, which downloads them on first use.
 */
@Singleton
public final class AttacheLibraryLoader implements LibraryLoader {
	private final LibraryManager libraries;

	@Inject
	public AttacheLibraryLoader(@NotNull LibraryManager libraries) {
		this.libraries = libraries;
	}

	@Override
	public void load(@NotNull String... groups) {
		libraries.loadGroups(groups);
	}
}
