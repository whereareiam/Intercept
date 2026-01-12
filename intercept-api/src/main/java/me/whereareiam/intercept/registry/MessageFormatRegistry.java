package me.whereareiam.intercept.registry;

import me.whereareiam.intercept.persistence.format.MessageFormat;

import java.util.Collection;
import java.util.Optional;

/**
 * Registry of message formats for a platform.
 */
public interface MessageFormatRegistry {
	/**
	 * Register a format.
	 *
	 * @param format format to register
	 */
	void register(MessageFormat format);

	/**
	 * Register a format and optionally mark it as default.
	 *
	 * @param format format to register
	 * @param setDefault whether to set as default format
	 */
	void register(MessageFormat format, boolean setDefault);

	/**
	 * Get a format by id.
	 *
	 * @param id format id
	 * @return format if registered
	 */
	Optional<MessageFormat> get(String id);

	/**
	 * Get the default format if one is set.
	 *
	 * @return default format
	 */
	Optional<MessageFormat> getDefault();

	/**
	 * Get all registered formats.
	 *
	 * @return formats collection
	 */
	Collection<MessageFormat> getAll();
}
