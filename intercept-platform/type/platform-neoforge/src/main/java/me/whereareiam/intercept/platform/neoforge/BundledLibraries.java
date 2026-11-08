package me.whereareiam.intercept.platform.neoforge;

import me.whereareiam.intercept.library.LibraryLoader;
import org.jetbrains.annotations.NotNull;

/**
 * The mod jar carries every library, so there is nothing to load.
 */
public final class BundledLibraries implements LibraryLoader {
	@Override
	public void load(@NotNull String... groups) {
	}
}
