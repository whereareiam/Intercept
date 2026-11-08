package me.whereareiam.intercept.platform.library;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.attache.LibraryManager;
import me.whereareiam.intercept.library.LibraryLoader;
import org.jetbrains.annotations.NotNull;

/**
 * Loads library groups through Attache, which downloads them on first use.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class AttacheLibraryLoader implements LibraryLoader {
	private final @NotNull LibraryManager libraries;

	@Override
	public void load(@NotNull String... groups) {
		libraries.loadGroups(groups);
	}
}
