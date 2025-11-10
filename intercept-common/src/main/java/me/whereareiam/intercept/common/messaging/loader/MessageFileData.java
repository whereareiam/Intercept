package me.whereareiam.intercept.common.messaging.loader;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.intercept.type.message.MessageType;

import java.util.Map;

/**
 * Represents the parsed data from a message file.
 * This is what we get after parsing YAML/JSON with Configura.
 */
@Getter
@Setter
public class MessageFileData {
	/**
	 * Optional file-level type (messages, templates, mixed)
	 * If present, all entries inherit this type unless they override it
	 */
	private MessageType type;

	/**
	 * Map of message key to message entry data
	 * Keys are relative to the file (e.g., "no-permission", "player-not-found")
	 * The scanner will prepend directory-based keys (e.g., "errors.permissions.")
	 */
	private Map<String, MessageEntryData> entries;
}