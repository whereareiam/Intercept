package me.whereareiam.intercept.platform.direct.common.persistence;

import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.util.MessageKeyUtil;

import java.nio.file.Path;
import java.util.Locale;

public final class DirectTranslationPathResolver {
	private final String extension;

	public DirectTranslationPathResolver(Format format) {
		this.extension = format == null ? "" : format.getExtension();
	}

	public boolean containsGlob(String path) {
		if (path == null) return false;
		return path.indexOf('*') >= 0 || path.indexOf('?') >= 0 || path.indexOf('[') >= 0;
	}

	public Path resolveGlobRoot(Path baseDirectory, String pattern) {
		String raw = pattern.replace('\\', '/');
		int wildcardIndex = findWildcardIndex(raw);
		if (wildcardIndex == -1) {
			Path path = Path.of(pattern);
			return path.isAbsolute() ? path : baseDirectory.resolve(path);
		}

		int slashIndex = raw.lastIndexOf('/', wildcardIndex);
		if (slashIndex <= 0) return baseDirectory;

		String basePart = raw.substring(0, slashIndex);
		Path base = Path.of(basePart);
		return base.isAbsolute() ? base : baseDirectory.resolve(base);
	}

	public boolean looksLikeFilePath(String rawPath, Locale defaultLocale) {
		String lower = rawPath.toLowerCase(Locale.ROOT);
		String extensionLower = extension.toLowerCase(Locale.ROOT);
		if (!extensionLower.isBlank() && lower.endsWith(extensionLower)) return true;

		Path path = Path.of(rawPath);
		String fileName = path.getFileName() == null ? rawPath : path.getFileName().toString();
		String withoutExt = stripExtension(fileName);
		if (MessageKeyUtil.parseLocaleToken(withoutExt, defaultLocale) != null) return true;

		return fileName.contains(".");
	}

	public Path resolveExistingPath(Path path) {
		if (path == null) return null;
		if (java.nio.file.Files.exists(path)) return path;
		String raw = path.toString();
		String lower = raw.toLowerCase(Locale.ROOT);
		String extensionLower = extension.toLowerCase(Locale.ROOT);
		if (!extensionLower.isBlank() && lower.endsWith(extensionLower)) return path;

		Path withExt = Path.of(raw + extension);
		if (java.nio.file.Files.exists(withExt)) return withExt;
		return path;
	}

	public Path resolvePathWithFormat(Path path) {
		if (path == null) return null;
		String raw = path.toString();
		String lower = raw.toLowerCase(Locale.ROOT);
		String extensionLower = extension.toLowerCase(Locale.ROOT);
		if (!extensionLower.isBlank() && lower.endsWith(extensionLower)) return path;
		return Path.of(raw + extension);
	}

	public String formatLocale(Locale locale) {
		if (locale == null) return "";
		StringBuilder builder = new StringBuilder(locale.getLanguage());
		if (!locale.getCountry().isEmpty()) builder.append('_').append(locale.getCountry());
		if (!locale.getVariant().isEmpty()) builder.append('_').append(locale.getVariant());
		return builder.toString();
	}

	private int findWildcardIndex(String raw) {
		int star = raw.indexOf('*');
		int question = raw.indexOf('?');
		int bracket = raw.indexOf('[');

		int idx = star >= 0 ? star : Integer.MAX_VALUE;
		if (question >= 0 && question < idx) idx = question;
		if (bracket >= 0 && bracket < idx) idx = bracket;

		return idx == Integer.MAX_VALUE ? -1 : idx;
	}

	private String stripExtension(String name) {
		int index = name.lastIndexOf('.');
		if (index <= 0) return name;
		return name.substring(0, index);
	}
}
