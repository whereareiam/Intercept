package me.whereareiam.intercept.platform.direct.oraylen.translation.file;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.persistence.file.DefaultTranslationFileCodecRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodec;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecScope;
import net.oraylen.api.Namespace;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public final class OraylenTranslationFileCodecRegistry implements TranslationFileCodecRegistry {
	private final TranslationFileCodecRegistry delegate;
	private final net.oraylen.api.translation.file.TranslationFileCodecRegistry oraylenRegistry;
	private final Map<String, TranslationFileCodec> adapters = new ConcurrentHashMap<>();

	@Inject
	public OraylenTranslationFileCodecRegistry(net.oraylen.api.translation.file.TranslationFileCodecRegistry oraylenRegistry) {
		this.delegate = new DefaultTranslationFileCodecRegistry();
		this.oraylenRegistry = oraylenRegistry;
	}

	@Override
	public void register(TranslationFileCodec codec, TranslationFileCodecScope scope, boolean setDefault) {
		delegate.register(codec, scope, setDefault);
	}

	@Override
	public void unregister(TranslationFileCodec codec) {
		delegate.unregister(codec);
	}

	@Override
	public Optional<TranslationFileCodec> resolveById(String id, String namespace) {
		Optional<TranslationFileCodec> resolved = delegate.resolveById(id, namespace);
		if (resolved.isPresent()) return resolved;
		if (oraylenRegistry == null) return Optional.empty();
		return oraylenRegistry.resolveById(normalizeId(id), toNamespace(namespace))
				.map(this::adapt);
	}

	@Override
	public Optional<TranslationFileCodec> resolveByExtension(String extension, String namespace) {
		Optional<TranslationFileCodec> resolved = delegate.resolveByExtension(extension, namespace);
		if (resolved.isPresent()) return resolved;
		if (oraylenRegistry == null) return Optional.empty();
		return oraylenRegistry.resolveByExtension(extension, toNamespace(namespace))
				.map(this::adapt);
	}

	@Override
	public TranslationFileCodec getDefault() {
		return delegate.getDefault();
	}

	@Override
	public Set<String> fileExtensions(String namespace) {
		Set<String> resolved = new LinkedHashSet<>();
		resolved.addAll(delegate.fileExtensions(namespace));
		if (oraylenRegistry != null) {
			Namespace oraylenNamespace = toNamespace(namespace);
			if (oraylenNamespace != null) {
				resolved.addAll(oraylenRegistry.fileExtensions(oraylenNamespace));
			}
		}
		return Set.copyOf(resolved);
	}

	private TranslationFileCodec adapt(net.oraylen.api.translation.file.TranslationFileCodec codec) {
		if (codec == null) return null;
		String id = normalizeId(codec.id());
		if (id == null) return null;
		return adapters.computeIfAbsent(id, ignored -> new OraylenTranslationFileCodecAdapter(codec));
	}

	private Namespace toNamespace(String namespace) {
		if (namespace == null || namespace.isBlank()) return null;
		if ("oraylen".equalsIgnoreCase(namespace)) return Namespace.internal();
		String lower = namespace.toLowerCase(Locale.ROOT);
		if (lower.startsWith("oraylen.plugin.")) {
			return Namespace.plugin(namespace.substring("oraylen.plugin.".length()));
		}
		if (lower.startsWith("oraylen.extension.")) {
			return Namespace.extension(namespace.substring("oraylen.extension.".length()));
		}
		return null;
	}

	private String normalizeId(String id) {
		if (id == null) return null;
		String trimmed = id.trim();
		return trimmed.isEmpty() ? null : trimmed.toUpperCase(Locale.ROOT);
	}
}
