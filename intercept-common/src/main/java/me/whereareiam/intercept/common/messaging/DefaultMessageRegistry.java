package me.whereareiam.intercept.common.messaging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;
import me.whereareiam.intercept.registry.base.Registry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Default implementation of MessageRegistry.
 * Thread-safe registry for message entries.
 */
@Singleton
public class DefaultMessageRegistry implements MessageRegistry, Reloadable {
	private final Map<String, CompiledMessageEntry> entries = new ConcurrentHashMap<>();

	@Inject
	public DefaultMessageRegistry(Registry<Reloadable> reloadableRegistry) {
		reloadableRegistry.register(this);
	}

	@Override
	public void register(String key, CompiledMessageEntry entry) {
		// Normalize hyphens to dots for consistent storage
		String normalizedKey = normalizeKey(key);
		entries.put(normalizedKey, entry);
	}

	@Override
	public CompiledMessageEntry get(String key) {
		return entries.get(key);
	}

	@Override
	public Set<String> getKeys() {
		return Set.copyOf(entries.keySet());
	}

	@Override
	public Set<String> getKeysByPrefix(String prefix) {
		return entries.keySet().stream()
				.filter(key -> key.startsWith(prefix))
				.collect(Collectors.toSet());
	}

	@Override
	public boolean exists(String key) {
		return entries.containsKey(key);
	}

	@Override
	public Map<String, CompiledMessageEntry> getAllEntries() {
		return Map.copyOf(entries);
	}

	@Override
	public void reload() {
		entries.clear();
	}

	/**
	 * Normalizes a key by converting hyphens to dots.
	 * This ensures consistent storage format (dots) regardless of input format.
	 *
	 * @param key the key to normalize
	 * @return the normalized key with dots instead of hyphens
	 */
	private String normalizeKey(String key) {
		return key.replace("-", ".");
	}
}