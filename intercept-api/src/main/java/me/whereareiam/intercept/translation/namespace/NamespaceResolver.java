package me.whereareiam.intercept.translation.namespace;

import java.nio.file.Path;
import java.util.Set;

/**
 * Resolves runtime namespaces and filesystem roots for translation storage.
 */
public interface NamespaceResolver {
	/**
	 * Resolve namespaces that should be loaded at runtime.
	 *
	 * @return resolved runtime namespaces
	 */
	Set<String> resolveRuntimeNamespaces();

	/**
	 * Resolve namespaces that should be used for storage.
	 *
	 * @return resolved storage namespaces
	 */
	Set<String> resolveStorageNamespaces();

	/**
	 * Determine whether namespaced layout should be used on disk.
	 *
	 * @return true if namespaced layout is enabled
	 */
	boolean usesNamespacedLayout();

	/**
	 * Resolve the root directory for a namespace.
	 *
	 * @param namespace namespace identifier
	 * @return resolved root path
	 */
	Path resolveNamespaceRoot(String namespace);
}
