package me.whereareiam.intercept.common.messaging.loader;

import me.whereareiam.intercept.common.messaging.DefaultMessageEntry;
import me.whereareiam.intercept.common.messaging.DefaultMessageRegistry;
import me.whereareiam.intercept.common.messaging.processor.TextProcessor;
import me.whereareiam.intercept.type.MessageType;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads message files into the MessageRegistry.
 * Converts file data into MessageEntry instances and registers them.
 */
public class MessageFileLoader {
	private final TextProcessor textProcessor;
	private final DefaultMessageRegistry registry;

	public MessageFileLoader(TextProcessor textProcessor, DefaultMessageRegistry registry) {
		this.textProcessor = textProcessor;
		this.registry = registry;
	}

	/**
	 * Load messages from parsed file data into the registry.
	 *
	 * @param keyPrefix the key prefix for this file (e.g., "errors.permissions")
	 * @param fileData  the parsed file data
	 */
	public void loadFromData(String keyPrefix, MessageFileData fileData) {
		MessageType fileType = fileData.getType();

		for (Map.Entry<String, MessageEntryData> entry : fileData.getEntries().entrySet()) {
			String entryKey = entry.getKey();
			MessageEntryData entryData = entry.getValue();

			// Build full key
			String fullKey = keyPrefix.isEmpty() ? entryKey : keyPrefix + "." + entryKey;

			// Convert to MessageEntry and register
			DefaultMessageEntry messageEntry = convertToMessageEntry(entryData, fileType);
			registry.register(fullKey, messageEntry);
		}
	}

	private DefaultMessageEntry convertToMessageEntry(MessageEntryData entryData, MessageType fileType) {
		// Determine type: entry type > file type > auto-detect
		MessageType type = entryData.getType();
		if (type == null) {
			type = fileType;
		}
		if (type == null) {
			type = autoDetectType(entryData);
		}

		// Convert text/translations
		if (entryData.getTranslations() != null && !entryData.getTranslations().isEmpty()) {
			// Multi-language message
			Map<String, String> processedTranslations = new HashMap<>();
			for (Map.Entry<String, Object> trans : entryData.getTranslations().entrySet()) {
				String locale = trans.getKey();
				String text = textProcessor.process(trans.getValue());
				processedTranslations.put(locale, text);
			}
			return new DefaultMessageEntry(type, processedTranslations);
		} else if (entryData.getText() != null) {
			// Single-language message or template
			String text = textProcessor.process(entryData.getText());
			return new DefaultMessageEntry(type, text);
		} else {
			throw new IllegalArgumentException("Message entry must have either 'text' or 'translations'");
		}
	}

	private MessageType autoDetectType(MessageEntryData entryData) {
		// If has translations, it's a message
		if (entryData.getTranslations() != null && !entryData.getTranslations().isEmpty()) {
			return MessageType.MESSAGE;
		}
		// If only has text, it's a template
		if (entryData.getText() != null) {
			return MessageType.TEMPLATE;
		}
		// Default to message
		return MessageType.MESSAGE;
	}
}