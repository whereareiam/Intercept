package me.whereareiam.intercept.common.translation.namespace;

import me.whereareiam.intercept.translation.namespace.NamespaceResolver;
import me.whereareiam.intercept.translation.PlatformNamespaceProvider;
import me.whereareiam.intercept.model.config.Settings;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultNamespaceResolverTest {
	@Test
	void resolveNamespacesUsesRuntimeSetAndLoadOverridesExtra() {
		Settings settings = new Settings();
		Settings.Translation.Namespaces namespaces = new Settings.Translation.Namespaces();
		namespaces.setExtra(List.of("shared", "proxy"));
		namespaces.setLoad(List.of("shared"));
		Settings.Translation translation = new Settings.Translation();
		translation.setNamespaces(namespaces);
		settings.setTranslation(translation);

		PlatformNamespaceProvider provider = new PlatformNamespaceProvider() {
			@Override
			public String getRuntimeNamespace() {
				return "oraylen";
			}

			@Override
			public Set<String> getRuntimeNamespaces() {
				return Set.of("oraylen", "oraylen.plugin.alpha");
			}
		};

		NamespaceResolver resolver = new DefaultNamespaceResolver(() -> settings, provider, Path.of("messages"));

		assertEquals(Set.of("oraylen", "oraylen.plugin.alpha", "shared"), resolver.resolveRuntimeNamespaces());
		assertEquals(Set.of("oraylen", "oraylen.plugin.alpha", "shared", "proxy"), resolver.resolveStorageNamespaces());
		assertTrue(resolver.usesNamespacedLayout());
	}

	@Test
	void resolveNamespacesSkipsInternalWhenProviderEmpty() {
		Settings settings = new Settings();
		Settings.Translation.Namespaces namespaces = new Settings.Translation.Namespaces();
		namespaces.setExtra(List.of());
		Settings.Translation translation = new Settings.Translation();
		translation.setNamespaces(namespaces);
		settings.setTranslation(translation);

		PlatformNamespaceProvider provider = new PlatformNamespaceProvider() {
			@Override
			public String getRuntimeNamespace() {
				return null;
			}

			@Override
			public Set<String> getRuntimeNamespaces() {
				return Set.of();
			}
		};

		NamespaceResolver resolver = new DefaultNamespaceResolver(() -> settings, provider, Path.of("messages"));

		assertTrue(resolver.resolveRuntimeNamespaces().isEmpty());
		assertTrue(resolver.resolveStorageNamespaces().isEmpty());
		assertFalse(resolver.usesNamespacedLayout());
	}
}
