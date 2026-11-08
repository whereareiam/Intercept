package me.whereareiam.intercept.platform;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import me.whereareiam.intercept.adapter.command.CommandConfiguration;
import me.whereareiam.intercept.adapter.persistence.PersistenceConfiguration;
import me.whereareiam.intercept.adapter.replication.ReplicationConfiguration;
import me.whereareiam.intercept.common.CommonConfiguration;
import me.whereareiam.intercept.interception.InterceptionConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Puts Intercept together on a platform: the shared parts, persistence, replication, interception and commands,
 * and the platform's own configuration.
 */
public final class InterceptInjector {
	private InterceptInjector() {
	}

	/**
	 * Creates the injector.
	 *
	 * @param platform the platform's configuration
	 * @param extra    further modules, such as an integration the platform found
	 * @return the injector
	 */
	public static @NotNull Injector create(@NotNull PlatformConfiguration platform, @NotNull Module... extra) {
		List<Module> modules = new ArrayList<>(List.of(
				new CommonConfiguration(),
				new PersistenceConfiguration(),
				new ReplicationConfiguration(),
				new InterceptionConfiguration(),
				new CommandConfiguration(),
				platform
		));
		modules.addAll(List.of(extra));

		return Guice.createInjector(modules);
	}
}
