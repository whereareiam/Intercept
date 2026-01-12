package me.whereareiam.intercept.persistence.format;

import me.whereareiam.intercept.registry.ReservedKeyRegistry;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Context for message format operations.
 */
public interface FormatContext {
	/**
	 * Root messages directory.
	 *
	 * @return root path
	 */
	Path getRoot();

	/**
	 * File being parsed or written.
	 *
	 * @return file path
	 */
	Path getFile();

	/**
	 * Default locale for this platform.
	 *
	 * @return default locale
	 */
	Locale getDefaultLocale();

	/**
	 * Namespace this file belongs to.
	 *
	 * @return namespace id
	 */
	String getNamespace();

	/**
	 * Reserved key registry used for extensions.
	 *
	 * @return reserved key registry
	 */
	ReservedKeyRegistry getReservedKeyRegistry();
}
