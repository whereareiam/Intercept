package me.whereareiam.intercept.platform.direct.oraylen.translation.file;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.persistence.file.TranslationFileCodec;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecResolver;
import me.whereareiam.intercept.platform.direct.oraylen.translation.OraylenTranslationRegistry;
import net.oraylen.api.Namespace;
import net.oraylen.api.translation.TranslationSource;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Singleton
public final class OraylenTranslationFileCodecResolver implements TranslationFileCodecResolver {
	private final OraylenTranslationRegistry translationRegistry;
	private final TranslationFileCodecRegistry codecRegistry;

	@Inject
	public OraylenTranslationFileCodecResolver(
			OraylenTranslationRegistry translationRegistry,
			TranslationFileCodecRegistry codecRegistry
	) {
		this.translationRegistry = translationRegistry;
		this.codecRegistry = codecRegistry;
	}

	@Override
	public TranslationFileCodec resolve(String namespace, String relativePath, String fileTypeId) {
		if (codecRegistry == null) return null;
		if (fileTypeId != null && !fileTypeId.isBlank()) {
			return codecRegistry.resolveById(fileTypeId, namespace)
					.orElse(codecRegistry.getDefault());
		}

		String sourceFileType = resolveFileTypeFromSources(namespace, relativePath);
		if (sourceFileType != null) {
			TranslationFileCodec resolved = codecRegistry.resolveById(sourceFileType, namespace).orElse(null);
			if (resolved != null) return resolved;
		}

		String extension = extractExtension(relativePath);
		if (extension != null) {
			return codecRegistry.resolveByExtension(extension, namespace)
					.orElse(codecRegistry.getDefault());
		}

		return codecRegistry.getDefault();
	}

	private String resolveFileTypeFromSources(String namespaceValue, String relativePath) {
		if (translationRegistry == null || relativePath == null || relativePath.isBlank()) return null;
		Namespace namespace = findNamespace(namespaceValue);
		if (namespace == null) return null;
		OraylenTranslationRegistry.NamespaceRegistration registration = translationRegistry.getNamespaceRegistration(namespace);
		if (registration == null || registration.source() == null) return null;

		List<TranslationSource.Source> sources = registration.source().sources();
		if (sources == null || sources.isEmpty()) return null;

		String normalized = normalizePath(relativePath);
		String singleType = resolveSingleFileType(sources);

		for (TranslationSource.Source source : sources) {
			if (source == null || source.path() == null || source.path().isBlank()) continue;
			if (matchesSource(normalized, namespaceValue, source)) {
				String fileType = source.fileType();
				if (fileType != null && !fileType.isBlank()) return fileType;
			}
		}

		return singleType;
	}

	private String resolveSingleFileType(List<TranslationSource.Source> sources) {
		Set<String> types = new LinkedHashSet<>();
		for (TranslationSource.Source source : sources) {
			if (source == null) continue;
			String fileType = source.fileType();
			if (fileType == null || fileType.isBlank()) continue;
			types.add(fileType);
		}
		return types.size() == 1 ? types.iterator().next() : null;
	}

	private boolean matchesSource(String relativePath, String namespace, TranslationSource.Source source) {
		String rawSource = source.path();
		if (rawSource == null || rawSource.isBlank()) return false;
		String sourcePath = normalizePath(rawSource);

		List<String> extensions = resolveExtensions(namespace, source);
		List<String> candidates = buildCandidates(relativePath, extensions);

		if (containsGlob(sourcePath)) {
			String pattern = toSystemPath(sourcePath);
			PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
			for (String candidate : candidates) {
				Path candidatePath = Path.of(toSystemPath(candidate));
				if (matcher.matches(candidatePath)) return true;
			}
			return false;
		}

		String sourceBase = stripTrailingSlash(stripExtension(sourcePath));
		for (String candidate : candidates) {
			String candidateBase = stripTrailingSlash(stripExtension(candidate));
			if (candidateBase.equals(sourceBase)) return true;
			if (candidateBase.startsWith(sourceBase + "/")) return true;
		}

		return false;
	}

	private List<String> buildCandidates(String relativePath, List<String> extensions) {
		List<String> candidates = new ArrayList<>();
		String normalized = normalizePath(relativePath);
		candidates.add(normalized);
		for (String extension : extensions) {
			if (extension == null || extension.isBlank()) continue;
			if (!normalized.toLowerCase(Locale.ROOT).endsWith(extension.toLowerCase(Locale.ROOT))) {
				candidates.add(normalized + extension);
			}
		}
		return candidates;
	}

	private List<String> resolveExtensions(String namespace, TranslationSource.Source source) {
		TranslationFileCodec codec = null;
		if (codecRegistry != null) {
			String fileType = source == null ? null : source.fileType();
			if (fileType != null && !fileType.isBlank()) {
				codec = codecRegistry.resolveById(fileType, namespace).orElse(null);
			}
			if (codec == null) {
				codec = codecRegistry.getDefault();
			}
		}
		if (codec != null && codec.getFileExtensions() != null && !codec.getFileExtensions().isEmpty()) {
			return codec.getFileExtensions();
		}
		return List.of(".yml");
	}

	private Namespace findNamespace(String namespaceValue) {
		if (namespaceValue == null || namespaceValue.isBlank()) return null;
		for (Namespace namespace : translationRegistry.getRegisteredNamespaces()) {
			if (namespace != null && namespaceValue.equals(namespace.value())) {
				return namespace;
			}
		}
		return null;
	}

	private String extractExtension(String path) {
		if (path == null || path.isBlank()) return null;
		String normalized = normalizePath(path);
		int lastSlash = normalized.lastIndexOf('/');
		String fileName = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex <= 0 || dotIndex == fileName.length() - 1) return null;
		return fileName.substring(dotIndex);
	}

	private boolean containsGlob(String path) {
		return path.indexOf('*') >= 0 || path.indexOf('?') >= 0 || path.indexOf('[') >= 0;
	}

	private String normalizePath(String path) {
		return path.replace('\\', '/');
	}

	private String toSystemPath(String path) {
		if (path == null) return null;
		return path.replace('/', java.io.File.separatorChar).replace('\\', java.io.File.separatorChar);
	}

	private String stripExtension(String path) {
		int index = path.lastIndexOf('.');
		if (index <= 0) return path;
		return path.substring(0, index);
	}

	private String stripTrailingSlash(String path) {
		if (path == null) return "";
		int length = path.length();
		while (length > 0 && path.charAt(length - 1) == '/') {
			length--;
		}
		return length == path.length() ? path : path.substring(0, length);
	}
}
