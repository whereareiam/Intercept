package me.whereareiam.intercept.persistence.file;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Scope for translation file codecs.
 * Can be global (all namespaces) or restricted to specific namespaces.
 */
public final class TranslationFileCodecScope {
	private final boolean global;
	private final Set<String> namespaces;

	private TranslationFileCodecScope(boolean global, Set<String> namespaces) {
		this.global = global;
		this.namespaces = namespaces == null ? Set.of() : Collections.unmodifiableSet(new HashSet<>(namespaces));
	}

	public static TranslationFileCodecScope global() {
		return new TranslationFileCodecScope(true, Set.of());
	}

	public static TranslationFileCodecScope namespace(String namespace) {
		return new TranslationFileCodecScope(false, namespace == null ? Set.of() : Set.of(namespace));
	}

	public static TranslationFileCodecScope namespaces(Set<String> namespaces) {
		return new TranslationFileCodecScope(false, namespaces);
	}

	public boolean isGlobal() {
		return global;
	}

	public boolean appliesTo(String namespace) {
		if (global) return true;
		if (namespace == null || namespace.isBlank()) return false;
		return namespaces.contains(namespace);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TranslationFileCodecScope that)) return false;
		return global == that.global && Objects.equals(namespaces, that.namespaces);
	}

	@Override
	public int hashCode() {
		return Objects.hash(global, namespaces);
	}
}
