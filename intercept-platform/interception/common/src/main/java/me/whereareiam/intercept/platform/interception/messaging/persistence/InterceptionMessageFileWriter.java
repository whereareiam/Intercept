package me.whereareiam.intercept.platform.interception.messaging.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.type.Format;
import me.whereareiam.configura.type.MultiValue;
import me.whereareiam.intercept.messaging.file.MessageFileWriter;
import me.whereareiam.intercept.platform.interception.messaging.MessageDocument;
import me.whereareiam.intercept.model.messaging.file.MapMessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.model.messaging.file.MessageValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class InterceptionMessageFileWriter implements MessageFileWriter {
	private static final String INTERCEPTION_EXTENSION_ID = "interception";
	private final Path messagesPath;

	@Inject
	public InterceptionMessageFileWriter(@Named("messagesPath") Path messagesPath) {
		this.messagesPath = messagesPath;
	}

	@Override
	public void write(String relativePath, MessageFileData fileData) {
		Path target = resolvePath(relativePath);

		try {
			Path parent = target.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}

			MessageDocument data = toDocument(fileData);
			Config.getDefaultWriter().write(target, data);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to write message persistence: " + target, e);
		}
	}

	@Override
	public Path resolvePath(String relativePath) {
		String normalized = relativePath.replace('\\', '/');
		Format format = Config.getDefaultWriter().getFormat();

		return messagesPath.resolve(normalized + format.getExtension()).normalize();
	}

	private MessageDocument toDocument(MessageFileData fileData) {
		MessageDocument document = new MessageDocument();
		if (fileData == null) return document;

		for (var entry : fileData.getEntries().entrySet()) {
			document.putEntry(entry.getKey(), toNode(entry.getValue()));
		}

		return document;
	}

	private MessageDocument.Node toNode(MessageFileData.Node node) {
		if (node instanceof MessageFileData.Entry entry) {
			return toEntry(entry);
		}
		if (node instanceof MessageFileData.Section section) {
			MessageDocument.Section out = new MessageDocument.Section();
			for (var entry : section.getEntries().entrySet())
				out.putEntry(entry.getKey(), toNode(entry.getValue()));

			return out;
		}

		return null;
	}

	private MessageDocument.Entry toEntry(MessageFileData.Entry entry) {
		MessageDocument.Entry out = new MessageDocument.Entry();
		out.setText(toMultiValue(entry.getText()));
		out.setLocales(toLocaleMap(entry.getLocales()));
		out.setInterception(toInterception(entry.getExtensions()));

		return out;
	}

	private Map<String, MultiValue<String>> toLocaleMap(Map<String, MessageValue> locales) {
		if (locales == null || locales.isEmpty()) return null;
		Map<String, MultiValue<String>> resolved = new LinkedHashMap<>();
		for (var entry : locales.entrySet()) {
			MultiValue<String> value = toMultiValue(entry.getValue());
			if (value != null) {
				resolved.put(entry.getKey(), value);
			}
		}

		return resolved;
	}

	private MessageDocument.Interception toInterception(MessageExtensions extensions) {
		if (extensions == null || extensions.isEmpty()) return null;

		var payload = extensions.get(INTERCEPTION_EXTENSION_ID);
		if (!(payload instanceof MapMessageExtensionPayload mapPayload)) return null;

		Map<String, Object> data = mapPayload.data();
		if (data.isEmpty()) return null;

		Object rawPatterns = data.get("patterns");
		if (!(rawPatterns instanceof List<?> patternsList)) return null;

		List<MessageDocument.Regex> patterns = new ArrayList<>();
		for (Object rawPattern : patternsList) {
			if (!(rawPattern instanceof Map<?, ?> patternMap)) continue;
			MessageDocument.Regex regex = toRegex(patternMap);
			if (regex != null)
				patterns.add(regex);
		}

		if (patterns.isEmpty()) return null;
		MessageDocument.Interception interception = new MessageDocument.Interception();
		interception.setPatterns(patterns);
		return interception;
	}

	private MessageDocument.Regex toRegex(Map<?, ?> patternMap) {
		Object patternValue = patternMap.get("pattern");
		if (!(patternValue instanceof String pattern)) return null;

		MessageDocument.Regex regex = new MessageDocument.Regex();
		regex.setPattern(pattern);

		Object priority = patternMap.get("priority");
		if (priority instanceof Number number)
			regex.setPriority(number.intValue());

		Object replaceMatched = patternMap.get("replaceMatched");
		if (replaceMatched instanceof Boolean bool)
			regex.setReplaceMatched(bool);

		Object placeholders = patternMap.get("placeholders");
		if (placeholders instanceof Map<?, ?> placeholderMap) {
			Map<String, String> resolved = new LinkedHashMap<>();
			placeholderMap.forEach((key, value) -> {
				if (key == null || value == null) return;
				resolved.put(String.valueOf(key), String.valueOf(value));
			});

			if (!resolved.isEmpty()) {
				regex.setPlaceholders(resolved);
			}
		}

		return regex;
	}

	private MultiValue<String> toMultiValue(MessageValue value) {
		if (value == null) return null;
		if (value instanceof MessageValue.Text text)
			return MultiValue.of(text.value());
		if (value instanceof MessageValue.Lines lines)
			return MultiValue.of(lines.values());

		return null;
	}
}
