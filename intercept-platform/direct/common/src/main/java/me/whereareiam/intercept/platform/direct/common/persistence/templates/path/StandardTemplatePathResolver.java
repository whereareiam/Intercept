package me.whereareiam.intercept.platform.direct.common.persistence.templates.path;

import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.platform.direct.common.persistence.DirectTranslationPathResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@RequiredArgsConstructor
public final class StandardTemplatePathResolver implements TemplatePathResolver {
	private final DirectTranslationPathResolver pathResolver;

	@Override
	public Path resolveLocaleTargetPath(Path baseDirectory, String rawPath, Locale locale, Locale defaultLocale) {
		String localeName = formatLocale(locale != null ? locale : defaultLocale);
		if (rawPath == null || rawPath.isBlank()) return null;

		if (containsGlob(rawPath)) {
			String replaced = rawPath.replace("*", localeName).replace("?", localeName);
			Path target = Path.of(replaced);
			return target.isAbsolute() ? target : baseDirectory.resolve(replaced);
		}

		Path resolvedPath = Path.of(rawPath);
		Path base = resolvedPath.isAbsolute() ? resolvedPath : baseDirectory.resolve(resolvedPath);

		boolean isDirectory = Files.isDirectory(base)
				|| (!Files.exists(base) && !pathResolver.looksLikeFilePath(rawPath, defaultLocale));

		if (isDirectory) {
			return base.resolve(localeName);
		}

		return base;
	}

	@Override
	public Path resolveTemplatePath(Path baseDirectory, String rawPath, String fallbackName, Locale defaultLocale) {
		if (rawPath == null || rawPath.isBlank()) return null;

		if (containsGlob(rawPath)) {
			String replaced = rawPath.replace("*", fallbackName).replace("?", fallbackName);
			Path target = Path.of(replaced);
			return target.isAbsolute() ? target : baseDirectory.resolve(replaced);
		}

		Path resolvedPath = Path.of(rawPath);
		Path base = resolvedPath.isAbsolute() ? resolvedPath : baseDirectory.resolve(resolvedPath);

		boolean isDirectory = Files.isDirectory(base)
				|| (!Files.exists(base) && !pathResolver.looksLikeFilePath(rawPath, defaultLocale));

		if (isDirectory) {
			return base.resolve(fallbackName);
		}

		return base;
	}

	@Override
	public Path resolvePathWithFormat(Path path) {
		return pathResolver.resolvePathWithFormat(path);
	}

	@Override
	public Path resolveExistingTarget(Path target, Path resolvedTarget) {
		if (resolvedTarget != null && Files.exists(resolvedTarget)) return resolvedTarget;
		if (target != null && Files.exists(target)) return target;
		return null;
	}

	@Override
	public boolean ensureParentDirectory(Path target) {
		if (target == null) return false;

		Path parent = target.getParent();
		if (parent == null) return true;

		try {
			Files.createDirectories(parent);
			return true;
		} catch (Exception e) {
			Logger.warn("Failed to create translation directory {}: {}", parent, e.getMessage());
			return false;
		}
	}

	@Override
	public String formatLocale(Locale locale) {
		return pathResolver.formatLocale(locale);
	}

	@Override
	public boolean containsGlob(String path) {
		return pathResolver.containsGlob(path);
	}
}
