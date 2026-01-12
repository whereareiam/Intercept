package me.whereareiam.intercept.translation;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Provides platform-specific namespace information.
 */
public interface PlatformNamespaceProvider {
	/**
	 * Runtime namespace for the current platform.
	 * <p>
	 * For platforms with multiple runtime namespaces, prefer {@link #getRuntimeNamespaces()}.
	 *
	 * @return namespace id
	 */
	String getRuntimeNamespace();

	/**
	 * Runtime namespaces for the current platform.
	 *
	 * @return set of namespace ids
	 */
	default Set<String> getRuntimeNamespaces() {
		String namespace = getRuntimeNamespace();
		if (namespace == null || namespace.isBlank()) return Set.of();
		return Set.of(namespace);
	}

	/**
	 * Optional custom namespace paths.
	 *
	 * @param messagesRoot base messages directory
	 * @return map of namespace to custom path
	 */
	default Map<String, Path> getNamespacePaths(Path messagesRoot) {
		return Map.of();
	}

	/**
	 * Whether downloads should use runtime namespaces instead of storage namespaces.
	 *
	 * @return true to use runtime namespaces for downloads
	 */
	default boolean useRuntimeNamespacesForDownload() {
		return false;
	}

	/**
	 * Whether a namespace should be written into the Intercept messages directory.
	 *
	 * @param namespace namespace id
	 * @return true if allowed
	 */
	default boolean isMessagesPathNamespaceAllowed(String namespace) {
		return true;
	}
}
