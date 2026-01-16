package me.whereareiam.intercept.platform.direct.oraylen.translation;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.intercept.translation.PlatformNamespaceProvider;
import me.whereareiam.intercept.platform.direct.common.persistence.DirectTranslationPathResolver;
import me.whereareiam.intercept.platform.direct.common.persistence.DirectTranslationSourceResolver;
import me.whereareiam.intercept.persistence.file.TranslationFileCodec;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecResolver;
import net.oraylen.api.Namespace;
import net.oraylen.api.translation.TranslationSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Namespace handling for Oraylen's direct platform.
 */
@Singleton
public class OraylenNamespaceProvider implements PlatformNamespaceProvider {
	private final OraylenTranslationRegistry registry;
	private final Provider<Locale> defaultLocaleProvider;
	private final TranslationFileCodecRegistry codecRegistry;
	private final TranslationFileCodecResolver codecResolver;

	@Inject
	public OraylenNamespaceProvider(
			OraylenTranslationRegistry registry,
			@Named("defaultLocale") Provider<Locale> defaultLocaleProvider,
			TranslationFileCodecRegistry codecRegistry,
			TranslationFileCodecResolver codecResolver
	) {
		this.registry = registry;
		this.defaultLocaleProvider = defaultLocaleProvider;
		this.codecRegistry = codecRegistry;
		this.codecResolver = codecResolver;
	}

	@Override
	public String getRuntimeNamespace() {
		for (String namespace : getRuntimeNamespaces()) {
			return namespace;
		}
		return null;
	}

	@Override
	public Set<String> getRuntimeNamespaces() {
		if (registry == null) return Set.of();
		Set<Namespace> namespaces = registry.getRegisteredNamespaces();
		if (namespaces.isEmpty()) return Set.of();

		Set<String> resolved = new LinkedHashSet<>();
		for (Namespace namespace : namespaces) {
			if (namespace == null) continue;
			String value = namespace.value();
			if (value.isBlank()) continue;
			resolved.add(value);
		}

		return resolved;
	}

	@Override
	public Map<String, Path> getNamespacePaths(Path messagesRoot) {
		return resolveNamespaceRoots();
	}

	@Override
	public boolean isMessagesPathNamespaceAllowed(String namespace) {
		if (namespace == null || namespace.isBlank()) return false;

		Set<String> runtime = getRuntimeNamespaces();
		if (runtime.isEmpty()) return true;

		boolean isRuntime = false;
		for (String runtimeNamespace : runtime) {
			if (runtimeNamespace != null && runtimeNamespace.equalsIgnoreCase(namespace)) {
				isRuntime = true;
				break;
			}
		}

		if (!isRuntime) return true;
		return true;
	}

	private Map<String, Path> resolveNamespaceRoots() {
		if (registry == null) return Map.of();
		Set<Namespace> namespaces = registry.getRegisteredNamespaces();
		if (namespaces.isEmpty()) return Map.of();

		Locale defaultLocale = defaultLocaleProvider.get();

		Map<String, Path> resolved = new LinkedHashMap<>();
		for (Namespace namespace : namespaces) {
			if (namespace == null) continue;
			OraylenTranslationRegistry.NamespaceRegistration registration = registry.getNamespaceRegistration(namespace);
			Path root = resolveNamespaceRoot(namespace, registration, defaultLocale);
			if (root != null) {
				resolved.put(namespace.value(), root);
			}
		}

		return resolved;
	}

	private Path resolveNamespaceRoot(
			Namespace namespace,
			OraylenTranslationRegistry.NamespaceRegistration registration,
			Locale defaultLocale
	) {
		if (registration == null || registration.baseDirectory() == null || registration.source() == null) {
			return null;
		}

		List<TranslationSource.Source> sources = registration.source().sources();
		if (sources == null || sources.isEmpty()) return null;

		List<TranslationSource.Source> candidates = new ArrayList<>();
		for (TranslationSource.Source source : sources) {
			if (source == null || source.path() == null || source.path().isBlank()) continue;
			candidates.add(source);
		}

		candidates.sort((left, right) -> Integer.compare(
				specificity(namespace, right, defaultLocale),
				specificity(namespace, left, defaultLocale)
		));

		for (TranslationSource.Source source : candidates) {
			TranslationFileCodec codec = resolveCodec(namespace, source);
			DirectTranslationPathResolver pathResolver = new DirectTranslationPathResolver(resolveExtensions(codec));
			DirectTranslationSourceResolver sourceResolver = new DirectTranslationSourceResolver(pathResolver);
			Path root = sourceResolver.resolveRoot(registration.baseDirectory(), source.path(), defaultLocale);
			if (root != null) return root;
		}

		return null;
	}

	private int specificity(Namespace namespace, TranslationSource.Source source, Locale defaultLocale) {
		if (source == null) return 0;
		TranslationFileCodec codec = resolveCodec(namespace, source);
		DirectTranslationPathResolver pathResolver = new DirectTranslationPathResolver(resolveExtensions(codec));
		DirectTranslationSourceResolver sourceResolver = new DirectTranslationSourceResolver(pathResolver);
		return sourceResolver.specificity(source.path(), defaultLocale);
	}

	private TranslationFileCodec resolveCodec(Namespace namespace, TranslationSource.Source source) {
		String fileType = source == null ? null : source.fileType();
		String path = source == null ? null : source.path();
		String namespaceValue = namespace == null ? null : namespace.value();
		if (codecResolver != null) {
			TranslationFileCodec resolved = codecResolver.resolve(namespaceValue, path, fileType);
			if (resolved != null) return resolved;
		}
		if (codecRegistry != null && fileType != null && !fileType.isBlank()) {
			TranslationFileCodec resolved = codecRegistry.resolveById(fileType, namespaceValue).orElse(null);
			if (resolved != null) return resolved;
		}
		return codecRegistry == null ? null : codecRegistry.getDefault();
	}

	private List<String> resolveExtensions(TranslationFileCodec codec) {
		if (codec != null && codec.getFileExtensions() != null && !codec.getFileExtensions().isEmpty()) {
			return codec.getFileExtensions();
		}
		TranslationFileCodec fallback = codecRegistry == null ? null : codecRegistry.getDefault();
		if (fallback != null && fallback.getFileExtensions() != null && !fallback.getFileExtensions().isEmpty()) {
			return fallback.getFileExtensions();
		}
		return List.of(".yml");
	}
}
