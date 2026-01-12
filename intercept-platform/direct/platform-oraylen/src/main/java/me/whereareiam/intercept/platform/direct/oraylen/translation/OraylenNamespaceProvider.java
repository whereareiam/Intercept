package me.whereareiam.intercept.platform.direct.oraylen.translation;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.translation.PlatformNamespaceProvider;
import me.whereareiam.intercept.platform.direct.common.persistence.DirectTranslationPathResolver;
import me.whereareiam.intercept.platform.direct.common.persistence.DirectTranslationSourceResolver;
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

	@Inject
	public OraylenNamespaceProvider(
			OraylenTranslationRegistry registry,
			@Named("defaultLocale") Provider<Locale> defaultLocaleProvider
	) {
		this.registry = registry;
		this.defaultLocaleProvider = defaultLocaleProvider;
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
		DirectTranslationPathResolver pathResolver = new DirectTranslationPathResolver(Config.getDefaultReader().getFormat());
		DirectTranslationSourceResolver sourceResolver = new DirectTranslationSourceResolver(
				pathResolver,
				Config.getDefaultReader().getFormat()
		);

		Map<String, Path> resolved = new LinkedHashMap<>();
		for (Namespace namespace : namespaces) {
			if (namespace == null) continue;
			OraylenTranslationRegistry.NamespaceRegistration registration = registry.getNamespaceRegistration(namespace);
			Path root = resolveNamespaceRoot(registration, defaultLocale, sourceResolver);
			if (root != null) {
				resolved.put(namespace.value(), root);
			}
		}

		return resolved;
	}

	private Path resolveNamespaceRoot(
			OraylenTranslationRegistry.NamespaceRegistration registration,
			Locale defaultLocale,
			DirectTranslationSourceResolver sourceResolver
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
				sourceResolver.specificity(right.path(), defaultLocale),
				sourceResolver.specificity(left.path(), defaultLocale)
		));

		for (TranslationSource.Source source : candidates) {
			Path root = sourceResolver.resolveRoot(registration.baseDirectory(), source.path(), defaultLocale);
			if (root != null) return root;
		}

		return null;
	}
}
