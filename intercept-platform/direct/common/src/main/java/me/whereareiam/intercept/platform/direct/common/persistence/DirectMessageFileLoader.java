package me.whereareiam.intercept.platform.direct.common.persistence;

import me.whereareiam.configura.Config;
import me.whereareiam.configura.node.Node;
import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.platform.direct.common.persistence.format.DirectFormatContext;
import me.whereareiam.intercept.persistence.format.MessageFormat;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.util.MessageKeyUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

public final class DirectMessageFileLoader {
	public MessageFileData load(
			Path root,
			Path file,
			String formatId,
			boolean applyPrefix,
			Locale defaultLocale,
			MessageFormatRegistry formatRegistry,
			ReservedKeyRegistry reservedKeyRegistry
	) {
		if (file == null || !Files.isRegularFile(file))
			return new MessageFileData();

		ObjectNode data;
		try {
			Node loaded = Config.getDefaultReader().readNode(file.toString());
			if (loaded instanceof ObjectNode objectNode) {
				data = objectNode;
			} else {
				data = new ObjectNode();
			}
		} catch (Exception e) {
			Logger.warn("Failed to load translation file {}: {}", file, e.getMessage());
			return new MessageFileData();
		}

		if (data.getValues().isEmpty())
			return new MessageFileData();

		if (formatRegistry == null || formatId == null || formatId.isBlank()) {
			Logger.warn("No message format registry available for file {}", file);
			return new MessageFileData();
		}

		MessageFormat messageFormat = formatRegistry.get(formatId).orElse(null);
		if (messageFormat == null) {
			Logger.warn("Unknown message format {} for file {}", formatId, file);
			return new MessageFileData();
		}

		DirectFormatContext context = new DirectFormatContext(
				root,
				file,
				defaultLocale,
				reservedKeyRegistry
		);

		MessageFileData fileData = messageFormat.parse(data, context);
		if (applyPrefix) {
			String prefix = MessageKeyUtil.buildKeyPrefix(root, file);
			return applyPrefix(fileData, prefix);
		}

		return fileData;
	}

	private MessageFileData applyPrefix(MessageFileData data, String prefix) {
		if (data == null || data.getEntries().isEmpty()) return data;
		if (prefix == null || prefix.isBlank()) return data;

		MessageFileData prefixed = new MessageFileData();
		applyPrefix(prefix, "", data.getEntries(), prefixed);
		return prefixed;
	}

	private void applyPrefix(
			String prefix,
			String current,
			Map<String, MessageFileData.Node> source,
			MessageFileData target
	) {
		for (Map.Entry<String, MessageFileData.Node> entry : source.entrySet()) {
			String key = entry.getKey();
			MessageFileData.Node node = entry.getValue();
			if (key == null || node == null) continue;

			String dotKey = current.isEmpty() ? key : current + "." + key;
			if (node instanceof MessageFileData.Entry fileEntry) {
				String fullKey = MessageKeyUtil.applyPrefix(prefix, dotKey);
				MessageKeyUtil.putEntry(target, fullKey, fileEntry);
				continue;
			}

			if (node instanceof MessageFileData.Section section) {
				applyPrefix(prefix, dotKey, section.getEntries(), target);
			}
		}
	}
}

