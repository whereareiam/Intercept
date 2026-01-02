package me.whereareiam.intercept.common.messaging.interception;

import com.google.inject.Singleton;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DefaultInterceptionRegistry implements InterceptionRegistry {
	private final Map<String, List<CompiledRegexPattern>> patterns = new ConcurrentHashMap<>();

	@Override
	public void register(String key, List<CompiledRegexPattern> patterns) {
		if (key == null || key.isBlank()) return;
		if (patterns == null || patterns.isEmpty()) {
			this.patterns.remove(key);
			return;
		}
		this.patterns.put(key, List.copyOf(patterns));
	}

	@Override
	public List<CompiledRegexPattern> get(String key) {
		return patterns.getOrDefault(key, List.of());
	}

	@Override
	public Set<String> getKeys() {
		return Set.copyOf(patterns.keySet());
	}

	@Override
	public Map<String, List<CompiledRegexPattern>> getAll() {
		return Map.copyOf(patterns);
	}

	@Override
	public void clear() {
		patterns.clear();
	}
}
