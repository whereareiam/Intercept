package me.whereareiam.intercept.platform.direct.common.persistence;

import me.whereareiam.intercept.util.MessageKeyUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public final class DirectTranslationPathResolver {
	private final List<String> extensions;
	private final String defaultExtension;

	public DirectTranslationPathResolver(Iterable<String> extensions) {
		this.extensions = normalizeExtensions(extensions);
		this.defaultExtension = resolveDefaultExtension(this.extensions);
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
		if (hasSupportedExtension(lower)) return true;

		Path path = Path.of(rawPath);
		String fileName = path.getFileName() == null ? rawPath : path.getFileName().toString();
		String withoutExt = stripExtension(fileName);
		if (MessageKeyUtil.parseLocaleToken(withoutExt, defaultLocale) != null) return true;

		return fileName.contains(".");
	}

	public Path resolveExistingPath(Path path) {
		if (path == null) return null;
		if (Files.exists(path)) return path;
		String raw = path.toString();
		String lower = raw.toLowerCase();
		if (hasSupportedExtension(lower)) return path;

		for (String extension : extensions) {
			if (extension == null || extension.isBlank()) continue;
			Path withExt = Path.of(raw + extension);
			if (Files.exists(withExt)) return withExt;
		}
		return path;
	}

	public Path resolvePathWithFormat(Path path) {
		if (path == null) return null;
		String raw = path.toString();
		String lower = raw.toLowerCase();
		if (hasSupportedExtension(lower)) return path;

		if (defaultExtension == null || defaultExtension.isBlank()) return path;

		return Path.of(raw + defaultExtension);
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

	public boolean hasSupportedExtension(Path path) {
		if (path == null) return false;
		String name = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase();
		return hasSupportedExtension(name);
	}

	private boolean hasSupportedExtension(String lowerName) {
		if (extensions.isEmpty()) return true;
		for (String extension : extensions) {
			if (extension == null || extension.isBlank()) continue;
			if (lowerName.endsWith(extension)) return true;
		}

		return false;
	}

	private List<String> normalizeExtensions(Iterable<String> rawExtensions) {
		if (rawExtensions == null) return List.of();
		LinkedHashSet<String> normalized = new LinkedHashSet<>();

		for (String extension : rawExtensions) {
			if (extension == null) continue;
			String trimmed = extension.trim();

			if (trimmed.isEmpty()) continue;
			String withDot = trimmed.startsWith(".") ? trimmed : "." + trimmed;
			normalized.add(withDot.toLowerCase());
		}

		if (normalized.isEmpty()) return List.of();

		return List.copyOf(normalized);
	}

	private String resolveDefaultExtension(List<String> extensions) {
		if (extensions == null || extensions.isEmpty()) return ".yml";
		String first = extensions.getFirst();

		return first == null || first.isBlank() ? ".yml" : first;
	}
}
