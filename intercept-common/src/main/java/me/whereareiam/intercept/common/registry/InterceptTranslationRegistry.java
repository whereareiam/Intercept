package me.whereareiam.intercept.common.registry;

import com.google.inject.Singleton;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.TranslationRegistry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class InterceptTranslationRegistry implements TranslationRegistry {
	private final Map<String, TranslationEntry> entries = new ConcurrentHashMap<>();

	@Override
	public void register(String key, TranslationEntry entry) {
		entries.put(normalize(key), entry);
	}

	@Override
	public void unregister(String key) {
		entries.remove(normalize(key));
	}

	@Override
	public TranslationEntry get(String key) {
		return entries.get(normalize(key));
	}

	@Override
	public Set<String> getKeys() {
		return Set.copyOf(entries.keySet());
	}

	@Override
	public Set<String> getKeys(String prefix) {
		String normalized = normalize(prefix);
		return entries.keySet().stream()
				.filter(key -> key.startsWith(normalized))
				.collect(Collectors.toSet());
	}

	@Override
	public boolean exists(String key) {
		return entries.containsKey(normalize(key));
	}

	@Override
	public Map<String, TranslationEntry> getAllEntries() {
		return Map.copyOf(entries);
	}

	@Override
	public void clear() {
		entries.clear();
	}

	private String normalize(String key) {
		if (key == null) return "";
		return key.replace("-", ".");
	}
}
