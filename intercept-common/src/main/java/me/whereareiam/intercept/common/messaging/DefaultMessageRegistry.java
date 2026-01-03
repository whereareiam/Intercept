package me.whereareiam.intercept.common.messaging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.TranslationRegistry;

import java.util.Map;
import java.util.Set;

/**
 * Default implementation of MessageRegistry.
 * Thread-safe registry for message entries.
 */
@Singleton
public class DefaultMessageRegistry implements MessageRegistry, Reloadable {
	private final TranslationRegistry registry;

	@Inject
	public DefaultMessageRegistry(
			TranslationRegistry registry,
			Registry<Reloadable> reloadableRegistry
	) {
		this.registry = registry;
		reloadableRegistry.register(this);
	}

	@Override
	public void register(String key, TranslationEntry entry) {
		registry.register(key, entry);
	}

	@Override
	public void unregister(String key) {
		registry.unregister(key);
	}

	@Override
	public TranslationEntry get(String key) {
		return registry.get(key);
	}

	@Override
	public Set<String> getKeys() {
		return registry.getKeys();
	}

	@Override
	public Set<String> getKeys(String prefix) {
		return registry.getKeys(prefix);
	}

	@Override
	public boolean exists(String key) {
		return registry.exists(key);
	}

	@Override
	public Map<String, TranslationEntry> getAllEntries() {
		return registry.getAllEntries();
	}

	@Override
	public void clear() {
		registry.clear();
	}

	@Override
	public void reload() {
		registry.clear();
	}
}
