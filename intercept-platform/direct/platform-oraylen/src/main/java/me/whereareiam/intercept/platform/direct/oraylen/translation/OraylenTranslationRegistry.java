package me.whereareiam.intercept.platform.direct.oraylen.translation;

import com.google.inject.Singleton;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.TranslationRegistry;
import net.oraylen.api.Namespace;
import net.oraylen.api.translation.TranslationSource;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public final class OraylenTranslationRegistry implements TranslationRegistry {
	private final Map<String, TranslationEntry> entries = new ConcurrentHashMap<>();
	private final Map<Namespace, NamespaceRegistration> namespaceRegistrations = new ConcurrentHashMap<>();

	@Override
	public boolean exists(String key) {
		return entries.containsKey(key);
	}

	@Override
	public void register(String key, TranslationEntry entry) {
		if (key == null || entry == null) return;
		entries.put(key, entry);
	}

	@Override
	public void unregister(String key) {
		if (key == null) return;
		entries.remove(key);
	}

	@Override
	public TranslationEntry get(String key) {
		if (key == null) return null;
		return entries.get(key);
	}

	@Override
	public Set<String> getKeys() {
		return Set.copyOf(entries.keySet());
	}

	@Override
	public Set<String> getKeys(String prefix) {
		if (prefix == null) return Set.of();
		return entries.keySet().stream()
				.filter(key -> key.startsWith(prefix))
				.collect(Collectors.toSet());
	}

	@Override
	public Map<String, TranslationEntry> getAllEntries() {
		return Map.copyOf(entries);
	}

	@Override
	public void clear() {
		entries.clear();
		namespaceRegistrations.clear();
	}

	public void registerNamespace(Namespace namespace, Path baseDirectory, TranslationSource source, Set<String> keys) {
		if (namespace == null || baseDirectory == null || source == null || keys == null) return;
		namespaceRegistrations.put(namespace, new NamespaceRegistration(baseDirectory, source, new HashSet<>(keys)));
	}

	public void unregisterNamespace(Namespace namespace) {
		NamespaceRegistration registration = namespaceRegistrations.remove(namespace);
		if (registration == null) return;

		for (String key : registration.keys())
			entries.remove(key);
	}

	public NamespaceRegistration getNamespaceRegistration(Namespace namespace) {
		return namespaceRegistrations.get(namespace);
	}

	public Set<Namespace> getRegisteredNamespaces() {
		return Set.copyOf(namespaceRegistrations.keySet());
	}

	public record NamespaceRegistration(Path baseDirectory, TranslationSource source, Set<String> keys) {
	}
}
