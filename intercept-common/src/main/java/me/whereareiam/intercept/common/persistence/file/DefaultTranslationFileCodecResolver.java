package me.whereareiam.intercept.common.persistence.file;

import com.google.inject.Inject;
import me.whereareiam.intercept.persistence.file.TranslationFileCodec;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecResolver;

public final class DefaultTranslationFileCodecResolver implements TranslationFileCodecResolver {
	private final TranslationFileCodecRegistry registry;

	@Inject
	public DefaultTranslationFileCodecResolver(TranslationFileCodecRegistry registry) {
		this.registry = registry;
	}

	@Override
	public TranslationFileCodec resolve(String namespace, String relativePath, String fileTypeId) {
		if (registry == null) return null;
		if (fileTypeId != null && !fileTypeId.isBlank()) {
			return registry.resolveById(fileTypeId, namespace)
					.orElse(registry.getDefault());
		}

		String extension = extractExtension(relativePath);
		if (extension != null) {
			return registry.resolveByExtension(extension, namespace)
					.orElse(registry.getDefault());
		}

		return registry.getDefault();
	}

	private String extractExtension(String path) {
		if (path == null || path.isBlank()) return null;
		String normalized = path.replace('\\', '/');
		int lastSlash = normalized.lastIndexOf('/');
		String fileName = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex <= 0 || dotIndex == fileName.length() - 1) return null;
		return fileName.substring(dotIndex);
	}
}
