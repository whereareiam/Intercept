package me.whereareiam.intercept.testing.environment;

import me.whereareiam.anvil.integration.junit.AnvilEnvironment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Starts a fresh network of two Intercept instances, {@link #A} and {@link #B}, on one platform around a test. Both
 * share a PostgreSQL database and Redis in containers and carry the texts of
 * {@link me.whereareiam.intercept.testing.fixture.Texts} as local messages. On Velocity each proxy has a Paper server
 * of its own behind it, {@link #LOBBY_A} and {@link #LOBBY_B}, which runs no Intercept. On Paper both servers also
 * run the probe, a plugin with a translator of its own.
 *
 * <pre>{@code
 * @Test
 * @InterceptNetwork(platform = Platform.VELOCITY)
 * void onVelocity(ScenarioContext anvil) {
 * }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@AnvilEnvironment(InterceptNetworkScenarios.class)
public @interface InterceptNetwork {
	/**
	 * The first instance, where players join unless a test says otherwise.
	 */
	String A = "intercept-a";

	/**
	 * The second instance.
	 */
	String B = "intercept-b";

	/**
	 * The Paper server behind {@link #A} on Velocity.
	 */
	String LOBBY_A = "lobby-a";

	/**
	 * The Paper server behind {@link #B} on Velocity.
	 */
	String LOBBY_B = "lobby-b";

	/**
	 * The platform both instances run on.
	 */
	Platform platform();

	/**
	 * Whether both instances download the remote bundles of {@link me.whereareiam.intercept.testing.fixture.Bundles}
	 * from a web server in a container.
	 */
	boolean remote() default false;
}
