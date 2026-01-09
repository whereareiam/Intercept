package me.whereareiam.intercept.util;

import me.whereareiam.intercept.Constants;

public final class NamespaceUtil {
	public static boolean hasNamespace(String key) {
		if (key == null || key.isEmpty()) return false;
		return key.indexOf(Constants.Namespace.NAMESPACE_SEPARATOR) > 0;
	}

	public static String getNamespace(String key) {
		if (!hasNamespace(key)) return null;
		int index = key.indexOf(Constants.Namespace.NAMESPACE_SEPARATOR);

		return index > 0 ? key.substring(0, index) : null;
	}

	public static String stripNamespace(String key) {
		if (!hasNamespace(key)) return key;
		return key.substring(key.indexOf(Constants.Namespace.NAMESPACE_SEPARATOR) + 1);
	}

	public static String qualify(String namespace, String key) {
		if (key == null) return null;
		if (hasNamespace(key)) return key;
		if (namespace == null || namespace.isBlank()) return key;

		return namespace + Constants.Namespace.NAMESPACE_SEPARATOR + key;
	}
}
