package me.whereareiam.intercept.common.provider.config;

import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.registry.base.Registry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ConfigProviderReloadTest {

	@TempDir
	Path tempDir;

	private Registry<Reloadable> mockRegistry;
	private TestConfigProvider provider;

	@BeforeEach
	void setUp() {
		mockRegistry = mock(Registry.class);
		provider = new TestConfigProvider(tempDir, mockRegistry);
	}

	@Test
	void shouldReloadValue() {
		// Get initial value
		String initial = provider.get();
		assertEquals("initial", initial);
		assertEquals(1, provider.getLoadCount());

		// Simulate change in underlying data
		provider.setNextValue("reloaded");

		// Reload
		provider.reload();

		// Get new value
		String reloaded = provider.get();
		assertEquals("reloaded", reloaded);
		assertEquals(2, provider.getLoadCount(), "Load should be called again after reload");
	}

	@Test
	void shouldRegisterTemplateOnlyOnce() {
		// First get
		provider.get();
		assertEquals(1, provider.getTemplateRegistrationCount(), "Template should be registered once");

		// Second get
		provider.get();
		assertEquals(1, provider.getTemplateRegistrationCount(), "Template should still be registered only once");

		// Reload
		provider.reload();
		assertEquals(1, provider.getTemplateRegistrationCount(), "Template should not be re-registered on reload");
	}

	@Test
	void shouldRegisterAsReloadable() {
		verify(mockRegistry).register(provider);
	}

	@Test
	void shouldHandleMultipleReloads() {
		provider.get();
		assertEquals("initial", provider.get());

		provider.setNextValue("reload1");
		provider.reload();
		assertEquals("reload1", provider.get());

		provider.setNextValue("reload2");
		provider.reload();
		assertEquals("reload2", provider.get());

		provider.setNextValue("reload3");
		provider.reload();
		assertEquals("reload3", provider.get());

		assertEquals(4, provider.getLoadCount(), "Should load initial + 3 reloads");
	}

	/**
	 * Test implementation of ConfigProvider for testing purposes
	 */
	private static class TestConfigProvider extends DefaultConfigProvider<String> {
		private String nextValue = "initial";
		private int loadCount = 0;
		private int templateRegistrationCount = 0;

		public TestConfigProvider(Path basePath, Registry<Reloadable> registry) {
			super(basePath, registry);
		}

		@Override
		protected String load() {
			loadCount++;
			return nextValue;
		}

		@Override
		protected void registerTemplate() {
			templateRegistrationCount++;
		}

		public void setNextValue(String value) {
			this.nextValue = value;
		}

		public int getLoadCount() {
			return loadCount;
		}

		public int getTemplateRegistrationCount() {
			return templateRegistrationCount;
		}
	}
}

