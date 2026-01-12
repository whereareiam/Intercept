package me.whereareiam.intercept.util;

import me.whereareiam.intercept.model.messaging.file.MessageFileData;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utilities for message key and locale handling without config-format dependencies.
 */
public final class MessageKeyUtil {
	public static Locale parseLocaleToken(String token, Locale defaultLocale) {
		if (token == null || token.isBlank()) return null;
		if ("default".equalsIgnoreCase(token)) return defaultLocale;

		String normalized = token.replace('-', '_');
		String[] parts = normalized.split("_", 3);
		if (parts.length == 0) return null;

		String language = parts[0];
		if (!isLanguageToken(language)) return null;

		String country = parts.length > 1 ? parts[1] : "";
		String variant = parts.length > 2 ? parts[2] : "";
		if (country.isEmpty() && variant.isEmpty() && defaultLocale != null) {
			if (language.equalsIgnoreCase(defaultLocale.getLanguage())) {
				country = defaultLocale.getCountry();
				variant = defaultLocale.getVariant();
			}
		}
		return new Locale(language, country, variant);
	}

	public static boolean isLocaleKey(String token, Locale defaultLocale) {
		return parseLocaleToken(token, defaultLocale) != null;
	}

	public static LocaleMatch detectLocaleFromPath(Path root, Path file, Locale defaultLocale) {
		if (root == null || file == null) return null;

		Path relative = root.relativize(file);
		List<String> parts = new ArrayList<>();
		for (Path part : relative) parts.add(part.toString());

		if (parts.isEmpty()) return null;

		String fileName = stripExtension(parts.get(parts.size() - 1));
		Locale localeFromFile = parseLocaleToken(fileName, defaultLocale);
		if (localeFromFile != null) {
			return new LocaleMatch(localeFromFile, parts.size() - 1);
		}

		for (int i = parts.size() - 2; i >= 0; i--) {
			Locale locale = parseLocaleToken(parts.get(i), defaultLocale);
			if (locale != null) {
				return new LocaleMatch(locale, i);
			}
		}

		return null;
	}

	public static String buildKeyPrefix(Path root, Path file) {
		if (root == null || file == null) return "";
		Path relative = root.relativize(file);
		String path = relative.toString();
		String withoutExt = stripExtension(path);

		return withoutExt.replace('\\', '.').replace('/', '.');
	}

	public static String buildKeyPrefixExcludingLocale(Path root, Path file, int localeSegmentIndex) {
		if (root == null || file == null) return "";

		Path relative = root.relativize(file);
		List<String> parts = new ArrayList<>();
		for (Path part : relative) {
			parts.add(part.toString());
		}

		if (parts.isEmpty()) return "";

		List<String> prefixParts = new ArrayList<>();
		for (int i = 0; i < parts.size(); i++) {
			if (i == localeSegmentIndex) continue;
			String segment = parts.get(i);
			if (i == parts.size() - 1)
				segment = stripExtension(segment);

			if (!segment.isBlank())
				prefixParts.add(segment);
		}

		return String.join(".", prefixParts);
	}

	public static String applyPrefix(String prefix, String key) {
		if (key == null || key.isBlank()) return prefix != null ? prefix : "";
		if (NamespaceUtil.hasNamespace(key)) return key;
		if (prefix == null || prefix.isBlank()) return key;

		return prefix + "." + key;
	}

	public static void putEntry(MessageFileData fileData, String dotKey, MessageFileData.Entry entry) {
		if (fileData == null || dotKey == null || dotKey.isBlank() || entry == null) return;
		putEntry(fileData.getEntries(), dotKey, entry);
	}

	public static void putEntry(Map<String, MessageFileData.Node> root, String dotKey, MessageFileData.Entry entry) {
		if (root == null || dotKey == null || dotKey.isBlank() || entry == null) return;

		String[] segments = dotKey.split("\\.");
		Map<String, MessageFileData.Node> current = root;

		for (int i = 0; i < segments.length; i++) {
			String segment = segments[i];
			if (segment.isEmpty()) continue;

			if (i == segments.length - 1) {
				current.put(segment, entry);
				return;
			}

			MessageFileData.Node existing = current.get(segment);
			MessageFileData.Section section;
			if (existing instanceof MessageFileData.Section existingSection) {
				section = existingSection;
			} else {
				section = new MessageFileData.Section();
				current.put(segment, section);
			}
			current = section.getEntries();
		}
	}

	private static String stripExtension(String name) {
		if (name == null) return "";
		int index = name.lastIndexOf('.');
		if (index <= 0) return name;
		return name.substring(0, index);
	}

	private static boolean isLanguageToken(String token) {
		if (token == null) return false;
		if (token.length() < 2 || token.length() > 3) return false;
		for (int i = 0; i < token.length(); i++) {
			char c = token.charAt(i);
			if (!Character.isLetter(c)) return false;
		}

		return true;
	}

	public record LocaleMatch(Locale locale, int segmentIndex) {
	}
}
