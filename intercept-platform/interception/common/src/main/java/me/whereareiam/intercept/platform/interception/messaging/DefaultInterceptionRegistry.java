package me.whereareiam.intercept.platform.interception.messaging;

import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultInterceptionRegistry implements InterceptionRegistry {
	private final Map<String, List<CompiledRegexPattern>> patterns = new ConcurrentHashMap<>();

	@Override
	public void register(String key, List<CompiledRegexPattern> patterns) {
		if (key == null || key.isBlank()) return;
		if (patterns == null || patterns.isEmpty()) {
			this.patterns.remove(key);
			return;
		}

		List<CompiledRegexPattern> copy = new ArrayList<>(patterns);
		this.patterns.put(key, Collections.unmodifiableList(copy));
	}

	@Override
	public List<CompiledRegexPattern> get(String key) {
		if (key == null) return List.of();
		return patterns.getOrDefault(key, List.of());
	}

	@Override
	public Set<String> getKeys() {
		return Set.copyOf(patterns.keySet());
	}

	@Override
	public Map<String, List<CompiledRegexPattern>> getAll() {
		return Map.copyOf(new HashMap<>(patterns));
	}

	@Override
	public void clear() {
		patterns.clear();
	}
}
