package me.whereareiam.intercept.common.translation;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.util.NamespaceUtil;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.semantica.model.translation.TranslationRequest;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.semantica.translation.base.TranslationLocale;
import me.whereareiam.semantica.model.RebuildMetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Singleton
public class NamespacedTranslationService implements TranslationService<Locale> {
	private final TranslationService<Locale> delegate;
	private final Provider<Settings> settingsProvider;

	@Inject
	public NamespacedTranslationService(
			TranslationService<Locale> delegate,
			Provider<Settings> settingsProvider
	) {
		this.delegate = delegate;
		this.settingsProvider = settingsProvider;
	}

	@Override
	public boolean exists(String key) {
		if (NamespaceUtil.hasNamespace(key)) {
			return delegate.exists(key);
		}

		List<String> matches = findMatchingNamespaces(key);
		if (matches.size() > 1) {
			warnDuplicate(key, matches);
		}

		return !matches.isEmpty();
	}

	@Override
	public String resolve(TranslationRequest<Locale> request) {
		if (request == null) return null;
		String key = resolveKey(request.getKey());
		return delegate.resolve(new TranslationRequest<>(
				key,
				request.getLocale(),
				request.getPlaceholders()
		));
	}

	@Override
	public String resolve(String key, Locale locale, Map<String, Object> placeholders) {
		String resolvedKey = resolveKey(key);
		return delegate.resolve(resolvedKey, locale, placeholders);
	}

	@Override
	public String resolve(String key, Locale locale) {
		String resolvedKey = resolveKey(key);
		return delegate.resolve(resolvedKey, locale);
	}

	@Override
	public void register(String key, TranslationEntry entry) {
		String resolvedKey = qualifyForWrite(key);
		delegate.register(resolvedKey, entry);
	}

	@Override
	public void register(Map<String, TranslationEntry> entries) {
		if (entries == null || entries.isEmpty()) return;

		Map<String, TranslationEntry> resolved = new HashMap<>();
		for (Map.Entry<String, TranslationEntry> entry : entries.entrySet()) {
			String key = qualifyForWrite(entry.getKey());
			resolved.put(key, entry.getValue());
		}
		delegate.register(resolved);
	}

	@Override
	public void unregister(String key) {
		String resolvedKey = resolveKey(key);
		delegate.unregister(resolvedKey);
	}

	@Override
	public int unregisterByPrefix(String prefix) {
		if (prefix == null) return 0;
		if (NamespaceUtil.hasNamespace(prefix)) {
			return delegate.unregisterByPrefix(prefix);
		}

		String namespace = Constants.Namespace.INTERNAL;
		if (prefix.isBlank()) {
			return unregisterByNamespace(namespace);
		}

		String resolvedPrefix = NamespaceUtil.qualify(namespace, prefix);
		return delegate.unregisterByPrefix(resolvedPrefix);
	}

	public int unregisterByNamespace(String namespace) {
		if (namespace == null || namespace.isBlank()) return 0;
		return delegate.unregisterByPrefix(namespace + Constants.Namespace.NAMESPACE_SEPARATOR);
	}

	@Override
	public Set<String> getKeys() {
		Set<String> keys = delegate.getKeys();
		return shouldAppendNamespace() ? keys : stripNamespaces(keys);
	}

	@Override
	public Set<String> getKeys(String prefix) {
		Set<String> keys;
		if (prefix == null || prefix.isBlank()) {
			keys = delegate.getKeys();
		} else if (NamespaceUtil.hasNamespace(prefix)) {
			keys = delegate.getKeys(prefix);
		} else {
			keys = new HashSet<>();
			for (String namespace : getOrderedNamespaces()) {
				if (namespace == null || namespace.isBlank()) continue;
				keys.addAll(delegate.getKeys(NamespaceUtil.qualify(namespace, prefix)));
			}
		}

		return shouldAppendNamespace() ? keys : stripNamespaces(keys);
	}

	@Override
	public Set<TranslationLocale> getAvailableLocales(String key) {
		if (NamespaceUtil.hasNamespace(key)) {
			return delegate.getAvailableLocales(key);
		}

		String resolvedKey = resolveKey(key);
		if (resolvedKey.equals(key)) {
			return Set.of();
		}
		return delegate.getAvailableLocales(resolvedKey);
	}

	@Override
	public void clearCache() {
		delegate.clearCache();
	}

	@Override
	public RebuildMetrics rebuild() {
		return delegate.rebuild();
	}

	private String resolveKey(String key) {
		if (NamespaceUtil.hasNamespace(key)) return key;

		List<String> matches = findMatchingNamespaces(key);
		if (matches.size() > 1) {
			warnDuplicate(key, matches);
		}

		if (matches.isEmpty()) return key;
		return NamespaceUtil.qualify(matches.get(0), key);
	}

	private List<String> findMatchingNamespaces(String key) {
		List<String> matches = new ArrayList<>();
		for (String namespace : getOrderedNamespaces()) {
			if (namespace == null || namespace.isBlank()) continue;
			String namespacedKey = NamespaceUtil.qualify(namespace, key);
			if (delegate.exists(namespacedKey)) {
				matches.add(namespace);
			}
		}
		return matches;
	}

	private List<String> getOrderedNamespaces() {
		Set<String> namespaces = new HashSet<>();
		for (String key : delegate.getKeys()) {
			String namespace = NamespaceUtil.getNamespace(key);
			if (namespace != null && !namespace.isBlank()) {
				namespaces.add(namespace);
			}
		}

		List<String> ordered = new ArrayList<>();
		ordered.add(Constants.Namespace.INTERNAL);
		namespaces.remove(Constants.Namespace.INTERNAL);

		List<String> remaining = new ArrayList<>(namespaces);
		remaining.sort(String.CASE_INSENSITIVE_ORDER);
		ordered.addAll(remaining);

		return ordered;
	}

	private String qualifyForWrite(String key) {
		if (NamespaceUtil.hasNamespace(key)) return key;
		return NamespaceUtil.qualify(Constants.Namespace.INTERNAL, key);
	}

	private boolean shouldAppendNamespace() {
		Settings settings = settingsProvider.get();
		if (settings == null) return false;

		Settings.Translation translation = settings.getTranslation();
		Settings.Translation.Namespaces namespaces = translation.getNamespaces();

		return namespaces.isAppendToKeys();
	}

	private Set<String> stripNamespaces(Set<String> keys) {
		Set<String> stripped = new HashSet<>();
		Map<String, String> owners = new HashMap<>();

		for (String key : keys) {
			String plain = NamespaceUtil.stripNamespace(key);
			String namespace = NamespaceUtil.getNamespace(key);

			String existing = owners.putIfAbsent(plain, namespace);
			if (existing != null && !existing.equals(namespace)) {
				Logger.warn("Multiple namespaces contain key '%s' (%s, %s).", plain, existing, namespace);
			}

			stripped.add(plain);
		}

		return stripped;
	}

	private void warnDuplicate(String key, List<String> matches) {
		if (matches.isEmpty()) return;
		Logger.warn("Multiple namespaces contain key '%s' (%s). Using '%s'.",
				key, String.join(", ", matches), matches.get(0));
	}
}
