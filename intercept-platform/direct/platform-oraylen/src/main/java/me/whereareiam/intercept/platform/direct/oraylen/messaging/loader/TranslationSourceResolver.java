package me.whereareiam.intercept.platform.direct.oraylen.messaging.loader;

import lombok.RequiredArgsConstructor;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.logging.Logger;
import net.oraylen.api.translation.TranslationSource;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

@RequiredArgsConstructor
final class TranslationSourceResolver {
	private final TranslationPathResolver pathResolver;
	private final Format format;

	ResolvedSource resolve(Path baseDirectory, TranslationSource.Source source, Locale defaultLocale) {
		String rawPath = source.path();
		if (rawPath == null || rawPath.isBlank()) {
			return new ResolvedSource(baseDirectory, baseDirectory, List.of(), rawPath);
		}

		Path raw = Path.of(rawPath);
		Path resolved = raw.isAbsolute() ? raw : baseDirectory.resolve(raw);

		if (pathResolver.containsGlob(rawPath)) {
			Path root = pathResolver.resolveGlobRoot(baseDirectory, rawPath);
			Path pattern = raw.isAbsolute() ? raw : baseDirectory.resolve(rawPath);
			return new ResolvedSource(baseDirectory, root, scanGlob(root, pattern), rawPath);
		}

		Path existing = pathResolver.resolveExistingPath(resolved);
		boolean treatAsDirectory = Files.isDirectory(existing)
				|| (!Files.exists(existing) && !pathResolver.looksLikeFilePath(rawPath, defaultLocale));

		if (treatAsDirectory) {
			return new ResolvedSource(baseDirectory, existing, scanDirectory(existing), rawPath);
		}

		Path root = existing.getParent() == null ? baseDirectory : existing.getParent();
		return new ResolvedSource(baseDirectory, root, Files.exists(existing) ? List.of(existing) : List.of(), rawPath);
	}

	int specificity(TranslationSource.Source source, Locale defaultLocale) {
		if (source == null) return 0;
		String path = source.path();
		if (path == null || path.isBlank()) return 0;

		String normalized = path.replace('\\', '/');
		String[] parts = normalized.split("/");
		int segments = 0;
		for (String part : parts) {
			if (!part.isBlank()) segments++;
		}

		int wildcardCount = 0;
		for (int i = 0; i < normalized.length(); i++) {
			char c = normalized.charAt(i);
			if (c == '*' || c == '?' || c == '[') wildcardCount++;
		}

		int fileBonus = pathResolver.looksLikeFilePath(path, defaultLocale) ? 5 : 0;
		return segments * 10 + fileBonus - wildcardCount * 20;
	}

	private List<Path> scanGlob(Path root, Path pattern) {
		if (root == null || pattern == null) return List.of();
		if (!Files.exists(root)) return List.of();

		Path absolutePattern = pattern.toAbsolutePath();
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + absolutePattern);

		try (Stream<Path> stream = Files.walk(root)) {
			return stream
					.filter(Files::isRegularFile)
					.filter(this::isNotHidden)
					.filter(this::hasSupportedExtension)
					.filter(path -> matcher.matches(path.toAbsolutePath()))
					.toList();
		} catch (Exception e) {
			Logger.warn("Failed to scan translation pattern {}: {}", pattern, e.getMessage());
			return List.of();
		}
	}

	private List<Path> scanDirectory(Path directory) {
		if (directory == null || !Files.isDirectory(directory)) return List.of();

		try (Stream<Path> stream = Files.walk(directory)) {
			return stream
					.filter(Files::isRegularFile)
					.filter(this::isNotHidden)
					.filter(this::hasSupportedExtension)
					.toList();
		} catch (Exception e) {
			Logger.warn("Failed to scan translation directory {}: {}", directory, e.getMessage());
			return List.of();
		}
	}

	private boolean isNotHidden(Path path) {
		try {
			if (path.getFileName() != null && path.getFileName().toString().startsWith(".")) {
				return false;
			}
			return !Files.isHidden(path);
		} catch (Exception ignored) {
			return true;
		}
	}

	private boolean hasSupportedExtension(Path path) {
		String name = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase(Locale.ROOT);
		String extension = format == null ? "" : format.getExtension();
		return extension != null && !extension.isBlank()
				&& name.endsWith(extension.toLowerCase(Locale.ROOT));
	}

	static final class ResolvedSource {
		private final Path baseDirectory;
		private final Path root;
		private final List<Path> files;
		private final String rawPath;

		ResolvedSource(Path baseDirectory, Path root, List<Path> files, String rawPath) {
			this.baseDirectory = Objects.requireNonNull(baseDirectory, "baseDirectory");
			this.root = Objects.requireNonNull(root, "root");
			this.files = files == null ? List.of() : List.copyOf(files);
			this.rawPath = rawPath;
		}

		Path baseDirectory() {
			return baseDirectory;
		}

		Path root() {
			return root;
		}

		List<Path> files() {
			return files;
		}

		String rawPath() {
			return rawPath;
		}
	}
}
