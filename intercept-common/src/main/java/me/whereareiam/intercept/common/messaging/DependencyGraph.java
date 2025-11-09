package me.whereareiam.intercept.common.messaging;

import me.whereareiam.intercept.common.util.MessageTags;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds and analyzes dependency graph for messages.
 * Detects circular dependencies and calculates optimal resolution order.
 */
public class DependencyGraph {
	private static final Pattern MESSAGE_REF_PATTERN = Pattern.compile("<" + MessageTags.MESSAGE_REF_PREFIX + ":([a-zA-Z0-9_.\\-]+)>");
	private static final Pattern TEMPLATE_PATTERN = Pattern.compile("<" + MessageTags.TEMPLATE_PREFIX + ":([a-zA-Z0-9_.\\-]+)");

	private final Map<String, Set<String>> dependencies = new HashMap<>();
	private List<String> resolutionOrder;

	/**
	 * Build the dependency graph from message entries.
	 *
	 * @param entries the message entries
	 */
	public void build(Map<String, DefaultMessageEntry> entries) {
		dependencies.clear();

		// Extract dependencies for each entry
		for (Map.Entry<String, DefaultMessageEntry> entry : entries.entrySet()) {
			String key = entry.getKey();
			String text = getTextForAnalysis(entry.getValue());
			Set<String> deps = extractDependencies(text);
			dependencies.put(key, deps);
		}

		// Calculate topological sort for resolution order
		calculateResolutionOrder();
	}

	/**
	 * Get dependencies for a specific key.
	 *
	 * @param key the message key
	 * @return set of dependency keys
	 */
	public Set<String> getDependencies(String key) {
		return dependencies.getOrDefault(key, Set.of());
	}

	/**
	 * Check if a key has circular dependencies.
	 *
	 * @param key the message key
	 * @return true if circular
	 */
	public boolean hasCircularDependency(String key) {
		return hasCircularDependency(key, new HashSet<>());
	}

	/**
	 * Get the resolution order (topological sort).
	 *
	 * @return list of keys in resolution order
	 */
	public List<String> getResolutionOrder() {
		return resolutionOrder != null ? resolutionOrder : List.of();
	}

	private boolean hasCircularDependency(String key, Set<String> visited) {
		if (visited.contains(key)) return true; // Circular reference detected

		Set<String> deps = dependencies.get(key);
		if (deps == null || deps.isEmpty()) return false;

		visited.add(key);

		for (String dep : deps)
			if (hasCircularDependency(dep, visited)) return true;

		visited.remove(key);

		return false;
	}

	private Set<String> extractDependencies(String text) {
		Set<String> deps = new HashSet<>();

		// Extract message references
		Matcher msgMatcher = MESSAGE_REF_PATTERN.matcher(text);
		while (msgMatcher.find())
			deps.add(msgMatcher.group(1));

		// Extract template references
		Matcher tplMatcher = TEMPLATE_PATTERN.matcher(text);
		while (tplMatcher.find())
			deps.add(tplMatcher.group(1));

		return deps;
	}

	private String getTextForAnalysis(DefaultMessageEntry entry) {
		// Get any text from the entry for analysis
		if (entry.getText() != null) return entry.getText();

		// For multi-language, just analyze one (dependencies are the same)
		if (entry.hasTranslations() && !entry.getLocales().isEmpty()) {
			String locale = entry.getLocales().iterator().next();
			return entry.getText(locale);
		}

		return "";
	}

	private void calculateResolutionOrder() {
		List<String> order = new ArrayList<>();
		Set<String> visited = new HashSet<>();
		Set<String> visiting = new HashSet<>();

		for (String key : dependencies.keySet())
			if (!visited.contains(key))
				topologicalSort(key, visited, visiting, order);

		resolutionOrder = order;
	}

	private void topologicalSort(String key, Set<String> visited, Set<String> visiting, List<String> order) {
		if (visiting.contains(key)) return; // Circular dependency, skip
		if (visited.contains(key)) return;

		visiting.add(key);

		Set<String> deps = dependencies.get(key);
		if (deps != null) for (String dep : deps)
			topologicalSort(dep, visited, visiting, order);

		visiting.remove(key);
		visited.add(key);
		order.add(key);
	}
}