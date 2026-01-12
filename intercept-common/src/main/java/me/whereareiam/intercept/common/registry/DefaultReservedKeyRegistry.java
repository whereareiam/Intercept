package me.whereareiam.intercept.common.registry;

import com.google.inject.Singleton;
import me.whereareiam.intercept.persistence.format.ReservedKeyHandler;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Default reserved key registry implementation.
 */
@Singleton
public class DefaultReservedKeyRegistry implements ReservedKeyRegistry {
	private static final Set<String> CORE_KEYS = Set.of("text", "locales");
	private final Map<String, ReservedKeyHandler> handlers = new LinkedHashMap<>();

	@Override
	public void register(ReservedKeyHandler handler) {
		if (handler == null || handler.getKey() == null || handler.getKey().isBlank()) return;
		handlers.put(normalize(handler.getKey()), handler);
	}

	@Override
	public void unregister(String key) {
		if (key == null || key.isBlank()) return;
		handlers.remove(normalize(key));
	}

	@Override
	public Optional<ReservedKeyHandler> get(String key) {
		if (key == null || key.isBlank()) return Optional.empty();
		return Optional.ofNullable(handlers.get(normalize(key)));
	}

	@Override
	public Set<String> getAllReservedKeys() {
		Set<String> keys = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		keys.addAll(CORE_KEYS);
		for (ReservedKeyHandler handler : handlers.values()) {
			if (handler == null || handler.getKey() == null || handler.getKey().isBlank()) continue;
			keys.add(handler.getKey());
		}
		return Set.copyOf(keys);
	}

	@Override
	public boolean isReservedKey(String key) {
		if (key == null || key.isBlank()) return false;
		String normalized = normalize(key);
		for (String core : CORE_KEYS) {
			if (normalize(core).equals(normalized)) {
				return true;
			}
		}
		return handlers.containsKey(normalized);
	}

	private String normalize(String key) {
		return key.trim().toLowerCase(Locale.ROOT);
	}
}
