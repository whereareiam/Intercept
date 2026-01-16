package me.whereareiam.intercept.common.persistence.file;

import me.whereareiam.intercept.persistence.file.TranslationFileCodec;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecScope;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultTranslationFileCodecRegistry implements TranslationFileCodecRegistry {
	private final Map<String, RegisteredCodec> codecsById = new ConcurrentHashMap<>();
	private final Map<String, Set<RegisteredCodec>> codecsByExtension = new ConcurrentHashMap<>();
	private volatile TranslationFileCodec defaultCodec;

	@Override
	public synchronized void register(TranslationFileCodec codec, TranslationFileCodecScope scope, boolean setDefault) {
		if (codec == null) return;
		String id = normalizeId(codec.getId());
		if (id == null || id.isBlank()) return;

		TranslationFileCodecScope resolvedScope = scope == null ? TranslationFileCodecScope.global() : scope;
		removeById(id);

		RegisteredCodec entry = new RegisteredCodec(codec, resolvedScope);
		codecsById.put(id, entry);
		for (String extension : codec.getFileExtensions()) {
			String normalized = normalizeExtension(extension);
			if (normalized == null) continue;
			codecsByExtension
					.computeIfAbsent(normalized, ignored -> new LinkedHashSet<>())
					.add(entry);
		}

		if (setDefault) {
			defaultCodec = codec;
		}
	}

	@Override
	public synchronized void unregister(TranslationFileCodec codec) {
		if (codec == null) return;
		String id = normalizeId(codec.getId());
		if (id == null || id.isBlank()) return;
		removeById(id);
		if (defaultCodec != null && id.equals(normalizeId(defaultCodec.getId()))) {
			defaultCodec = null;
		}
	}

	@Override
	public Optional<TranslationFileCodec> resolveById(String id, String namespace) {
		if (id == null || id.isBlank()) return Optional.empty();
		RegisteredCodec registered = codecsById.get(normalizeId(id));
		if (registered == null) return Optional.empty();
		return registered.scope().appliesTo(namespace) ? Optional.of(registered.codec()) : Optional.empty();
	}

	@Override
	public Optional<TranslationFileCodec> resolveByExtension(String extension, String namespace) {
		String normalized = normalizeExtension(extension);
		if (normalized == null) return Optional.empty();
		Set<RegisteredCodec> entries = codecsByExtension.get(normalized);
		if (entries == null || entries.isEmpty()) return Optional.empty();
		for (RegisteredCodec entry : entries) {
			if (entry.scope().appliesTo(namespace)) {
				return Optional.of(entry.codec());
			}
		}
		return Optional.empty();
	}

	@Override
	public TranslationFileCodec getDefault() {
		return defaultCodec;
	}

	@Override
	public Set<String> fileExtensions(String namespace) {
		Set<String> extensions = new HashSet<>();
		for (Set<RegisteredCodec> entries : codecsByExtension.values()) {
			for (RegisteredCodec entry : entries) {
				if (entry.scope().appliesTo(namespace)) {
					extensions.addAll(entry.codec().getFileExtensions());
				}
			}
		}
		return extensions;
	}

	private void removeById(String id) {
		RegisteredCodec existing = codecsById.remove(id);
		if (existing == null) return;
		for (String extension : existing.codec().getFileExtensions()) {
			String normalized = normalizeExtension(extension);
			if (normalized == null) continue;
			Set<RegisteredCodec> entries = codecsByExtension.get(normalized);
			if (entries == null) continue;
			entries.remove(existing);
			if (entries.isEmpty()) {
				codecsByExtension.remove(normalized);
			}
		}
	}

	private String normalizeId(String id) {
		if (id == null) return null;
		String trimmed = id.trim();
		return trimmed.isEmpty() ? null : trimmed.toUpperCase(Locale.ROOT);
	}

	private String normalizeExtension(String extension) {
		if (extension == null) return null;
		String trimmed = extension.trim();
		if (trimmed.isEmpty()) return null;
		String normalized = trimmed.startsWith(".") ? trimmed : "." + trimmed;
		return normalized.toLowerCase(Locale.ROOT);
	}

	private record RegisteredCodec(TranslationFileCodec codec, TranslationFileCodecScope scope) {
	}
}
