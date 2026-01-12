package me.whereareiam.intercept.common.translation.namespace;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.translation.PlatformNamespaceProvider;
import me.whereareiam.intercept.translation.namespace.NamespaceResolver;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves runtime and storage namespaces based on settings and platform defaults.
 */
public class DefaultNamespaceResolver implements NamespaceResolver {
	private final Provider<Settings> settingsProvider;
	private final PlatformNamespaceProvider platformNamespaceProvider;
	private final Path messagesRoot;

	@Inject
	public DefaultNamespaceResolver(
			Provider<Settings> settingsProvider,
			PlatformNamespaceProvider platformNamespaceProvider,
			@Named("messagesPath") Path messagesRoot
	) {
		this.settingsProvider = settingsProvider;
		this.platformNamespaceProvider = platformNamespaceProvider;
		this.messagesRoot = messagesRoot;
	}

	@Override
	public Set<String> resolveRuntimeNamespaces() {
		Settings.Translation.Namespaces namespaces = resolveSettings();

		List<String> extras = namespaces != null ? namespaces.getExtra() : List.of();
		List<String> load = namespaces != null ? namespaces.getLoad() : List.of();

		Set<String> runtime = new LinkedHashSet<>(resolvePlatformNamespaces());

		if (load != null && !load.isEmpty()) {
			runtime.addAll(filterValid(load));
		} else if (extras != null && !extras.isEmpty()) {
			runtime.addAll(filterValid(extras));
		}

		return runtime;
	}

	@Override
	public Set<String> resolveStorageNamespaces() {
		Settings.Translation.Namespaces namespaces = resolveSettings();
		List<String> extras = namespaces != null ? namespaces.getExtra() : List.of();

		Set<String> storage = new LinkedHashSet<>(resolvePlatformNamespaces());
		if (extras != null && !extras.isEmpty()) {
			storage.addAll(filterValid(extras));
		}

		return storage;
	}

	@Override
	public boolean usesNamespacedLayout() {
		Settings.Translation.Namespaces namespaces = resolveSettings();
		List<String> extras = namespaces != null ? namespaces.getExtra() : List.of();
		if (extras == null || extras.isEmpty()) return false;
		for (String extra : extras) {
			if (extra != null && !extra.isBlank()) return true;
		}
		return false;
	}

	@Override
	public Path resolveNamespaceRoot(String namespace) {
		if (messagesRoot == null) return null;

		if (platformNamespaceProvider != null) {
			Map<String, Path> custom = platformNamespaceProvider.getNamespacePaths(messagesRoot);
			if (custom != null && namespace != null && custom.containsKey(namespace)) {
				return custom.get(namespace);
			}
		}

		boolean namespacedLayout = usesNamespacedLayout();
		if (namespacedLayout && namespace != null && !namespace.isBlank()) {
			return messagesRoot.resolve(namespace);
		}

		return messagesRoot;
	}

	private Settings.Translation.Namespaces resolveSettings() {
		Settings settings = settingsProvider == null ? null : settingsProvider.get();
		Settings.Translation translation = settings != null ? settings.getTranslation() : null;
		return translation != null ? translation.getNamespaces() : null;
	}

	private Set<String> resolvePlatformNamespaces() {
		if (platformNamespaceProvider == null) return Set.of(Constants.Namespace.INTERNAL);
		return filterValid(platformNamespaceProvider.getRuntimeNamespaces());
	}

	private Set<String> filterValid(List<String> values) {
		Set<String> filtered = new LinkedHashSet<>();
		for (String value : values) {
			if (value == null || value.isBlank()) continue;
			filtered.add(value);
		}
		return filtered;
	}

	private Set<String> filterValid(Set<String> values) {
		Set<String> filtered = new LinkedHashSet<>();
		if (values == null || values.isEmpty()) return filtered;
		for (String value : values) {
			if (value == null || value.isBlank()) continue;
			filtered.add(value);
		}
		return filtered;
	}
}
