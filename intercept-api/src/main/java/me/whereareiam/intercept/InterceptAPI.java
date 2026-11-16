package me.whereareiam.intercept;

import me.whereareiam.intercept.language.LanguageService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Intercept's API: what the running Intercept offers to other plugins and mods, the same on Paper, Velocity and
 * NeoForge.
 *
 * <p>Obtain it through {@link #find()}, {@link #get()} or {@link #whenReady(Consumer)} and pass it to the classes
 * that need it, which then depend on this interface instead of static calls and can be given a mock in tests:</p>
 *
 * <pre>{@code
 * Locale language = InterceptAPI.find()
 *         .flatMap(intercept -> intercept.languages().chosen(player.getUniqueId()))
 *         .orElse(player.locale());
 * }</pre>
 *
 * <p>Intercept registers its API as its core starts, before it intercepts messages and handles players, and removes
 * it when it stops. {@link #whenReady(Consumer)} waits until Intercept begins handling players.</p>
 *
 * <p>Its classes keep their names in every jar, so a plugin compiles against {@code me.whereareiam.intercept:api}
 * and leaves it out of its own jar. On Paper the plugin declares Intercept as a dependency, so it sees Intercept's
 * classes.</p>
 */
public interface InterceptAPI {
	/**
	 * Returns the API of the running Intercept. Use it when your plugin starts after Intercept.
	 *
	 * @return the API of the running instance
	 * @throws IllegalStateException when Intercept is not running
	 */
	static @NotNull InterceptAPI get() {
		return RunningInstance.get();
	}

	/**
	 * Returns the API of the running Intercept, or empty when Intercept is not running.
	 *
	 * @return the API of the running instance, if any
	 */
	static @NotNull Optional<InterceptAPI> find() {
		return RunningInstance.find();
	}

	/**
	 * Runs the action with the API once Intercept is ready: its core, interception and integrations have started,
	 * and it begins handling players. When it already is, the action runs immediately. A waiting action runs once, on the thread
	 * that starts Intercept; one that fails is logged and keeps neither Intercept nor the other actions from
	 * starting.
	 *
	 * @param action the action to run with the API
	 */
	static void whenReady(@NotNull Consumer<InterceptAPI> action) {
		RunningInstance.whenReady(action);
	}

	/**
	 * Registers the API of the running instance, which {@link #get()} returns from then on. Called by Intercept as
	 * its core starts; other plugins never call it.
	 *
	 * @param intercept the API of the running instance
	 * @throws IllegalStateException when an instance is already registered
	 */
	@ApiStatus.Internal
	static void register(@NotNull InterceptAPI intercept) {
		RunningInstance.register(intercept);
	}

	/**
	 * Marks Intercept as ready, as it begins handling players, and runs the actions waiting in
	 * {@link #whenReady(Consumer)}. Called by Intercept during startup; other plugins never call it.
	 *
	 * @throws IllegalStateException when no instance is registered
	 */
	@ApiStatus.Internal
	static void markReady() {
		RunningInstance.markReady();
	}

	/**
	 * Removes the API of the running instance. Called by Intercept when it stops; other plugins never call it.
	 */
	@ApiStatus.Internal
	static void unregister() {
		RunningInstance.unregister();
	}

	/**
	 * Returns the languages players chose: a player's choice is the same on every server of the network.
	 *
	 * <pre>{@code
	 * Optional<Locale> chosen = intercept.languages().chosen(player.getUniqueId());
	 * }</pre>
	 *
	 * @return the language service
	 */
	@NotNull LanguageService languages();
}
