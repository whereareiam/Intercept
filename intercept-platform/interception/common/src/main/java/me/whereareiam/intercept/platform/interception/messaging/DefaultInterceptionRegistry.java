package me.whereareiam.intercept.platform.interception.messaging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.model.regex.CompiledRegexPattern;
import me.whereareiam.intercept.registry.base.Registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DefaultInterceptionRegistry implements InterceptionRegistry, Reloadable {
	private final Map<String, List<CompiledRegexPattern>> patterns = new ConcurrentHashMap<>();

	@Inject
	public DefaultInterceptionRegistry(
			Registry<Reloadable> reloadableRegistry
	) {
		reloadableRegistry.register(this);
	}

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

	@Override
	public void clearNamespace(String namespace) {
		if (namespace == null || namespace.isBlank()) return;

		String prefix = namespace + Constants.Namespace.NAMESPACE_SEPARATOR;
		patterns.keySet().removeIf(key -> key.startsWith(prefix));
	}

	@Override
	public void reload() {
		clearNamespace(Constants.Namespace.INTERNAL);
	}
}
