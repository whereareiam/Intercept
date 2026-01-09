package me.whereareiam.intercept.common.config;

import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.provider.config.DefaultConfigProvider;
import me.whereareiam.intercept.registry.base.Registry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConfigProviderReloadTest {
	@Mock
	private Registry<Reloadable> registry;

	@TempDir
	Path tempDir;

	private TestConfigProvider provider;

	@BeforeEach
	void setUp() {
		provider = new TestConfigProvider(tempDir, registry);
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
	void shouldRegisterAsReloadable() {
		verify(registry).register(provider);
	}

	@Test
	void shouldHandleMultipleReloads() {
		provider.get();
		Assertions.assertEquals("initial", provider.get());

		provider.setNextValue("reload1");
		provider.reload();
		Assertions.assertEquals("reload1", provider.get());

		provider.setNextValue("reload2");
		provider.reload();
		Assertions.assertEquals("reload2", provider.get());

		provider.setNextValue("reload3");
		provider.reload();
		Assertions.assertEquals("reload3", provider.get());

		assertEquals(4, provider.getLoadCount(), "Should load initial + 3 reloads");
	}

	/**
	 * Test implementation of ConfigProvider for testing purposes
	 */
	private static class TestConfigProvider extends DefaultConfigProvider<String> {
		private String nextValue = "initial";
		private int loadCount = 0;

		public TestConfigProvider(Path basePath, Registry<Reloadable> registry) {
			super(basePath, registry);
		}

		@Override
		protected String load() {
			loadCount++;
			return nextValue;
		}

		public void setNextValue(String value) {
			this.nextValue = value;
		}

		public int getLoadCount() {
			return loadCount;
		}
	}
}

