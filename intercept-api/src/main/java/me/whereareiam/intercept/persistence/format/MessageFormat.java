package me.whereareiam.intercept.persistence.format;

import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;

/**
 * Defines how message files are parsed and written.
 * Formats are registered per-platform and identified by ID.
 */
@SuppressWarnings("unused")
public interface MessageFormat {
	/**
	 * Unique format identifier (e.g., "LOCALE", "MULTI_LOCALE", "TEMPLATE").
	 *
	 * @return format id
	 */
	String getId();

	/**
	 * Parse raw file data into a MessageFileData structure.
	 *
	 * @param rawData raw config data
	 * @param context format context
	 * @return parsed file data
	 */
	MessageFileData parse(ObjectNode rawData, FormatContext context);

	/**
	 * Write MessageFileData into raw config data.
	 *
	 * @param data message file data
	 * @param context format context
	 * @return raw config data suitable for writing
	 */
	ObjectNode write(MessageFileData data, FormatContext context);

	/**
	 * Whether this format can represent the provided entry.
	 *
	 * @param entry entry to check
	 * @return true if representable
	 */
	boolean canRepresent(MessageFileData.Entry entry);
}
