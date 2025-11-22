package me.whereareiam.intercept.common;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Factory for creating Guice injectors with conditional module installation.
 * Ensures that CommonConfiguration is fully initialized before checking conditions,
 * allowing configs to be available when deciding whether to install conditional modules.
 */
public final class InjectorFactory {
	/**
	 * Represents a conditional module that should be installed only if its condition is met.
	 *
	 * @param condition      predicate that receives the base injector and returns true if the module should be installed
	 * @param moduleSupplier supplier for the module to install (must not be null)
	 */
	public record ConditionalModule(
			Predicate<Injector> condition,
			Supplier<Module> moduleSupplier
	) {
		public ConditionalModule {
			if (moduleSupplier == null)
				throw new IllegalArgumentException("moduleSupplier must not be null");
		}
	}

	/**
	 * Creates a Guice injector with the provided modules, conditionally adding modules
	 * based on their conditions.
	 * <p>
	 * This method ensures that CommonConfiguration has fully initialized before
	 * checking any conditions, allowing configs to be available when deciding
	 * whether to install conditional modules.
	 *
	 * @param dataPath           the data path for CommonConfiguration
	 * @param conditionalModules list of conditional modules to potentially install
	 * @param modules            the base modules to include (should include CommonConfiguration,
	 *                           or it will be added automatically)
	 * @return a configured injector with conditional modules installed as needed
	 */
	public static Injector createInjector(
			Path dataPath,
			List<ConditionalModule> conditionalModules,
			Module... modules
	) {
		List<Module> baseModules = new ArrayList<>();
		boolean hasCommonConfig = false;

		for (Module module : modules) {
			baseModules.add(module);
			if (module instanceof CommonConfiguration) hasCommonConfig = true;
		}

		if (!hasCommonConfig) baseModules.add(new CommonConfiguration(dataPath));

		Injector baseInjector = Guice.createInjector(baseModules);

		if (conditionalModules == null || conditionalModules.isEmpty())
			return baseInjector;

		List<Module> modulesToInstall = new ArrayList<>();
		for (ConditionalModule conditional : conditionalModules) {
			if (conditional.condition().test(baseInjector))
				modulesToInstall.add(conditional.moduleSupplier().get());
		}

		if (modulesToInstall.isEmpty()) return baseInjector;

		return baseInjector.createChildInjector(modulesToInstall);
	}
}