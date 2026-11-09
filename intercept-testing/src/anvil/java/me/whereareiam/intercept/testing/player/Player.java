package me.whereareiam.intercept.testing.player;

import me.whereareiam.anvil.api.model.player.PlayerConnection;
import me.whereareiam.anvil.api.model.player.PlayerLogin;
import me.whereareiam.anvil.api.model.player.PlayerOptions;
import me.whereareiam.anvil.api.player.SimulatedPlayer;
import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.anvil.capability.messages.MessageWatch;
import me.whereareiam.anvil.capability.messages.Messages;
import me.whereareiam.anvil.capability.session.Session;

import java.time.Duration;

/**
 * A player connected to one instance of a test network.
 *
 * <pre>{@code
 * Player alice = Player.join(anvil, "Alice", InterceptNetwork.A);
 * alice.command("language de").sees(Texts.GERMAN_CHOSEN);
 * }</pre>
 */
public final class Player {
	/**
	 * How long a player waits for a text.
	 */
	public static final Duration SOON = Duration.ofSeconds(30);

	private final Messages messages;
	private MessageWatch watch;

	private Player(SimulatedPlayer player) {
		this.messages = player.capability(Messages.class);
		this.watch = messages.watch();
	}

	/**
	 * Connects a player to one instance. The same name on two instances is the same player, connected twice.
	 *
	 * @param anvil    the network
	 * @param name     the player's name
	 * @param instance the server or proxy to connect to
	 * @return the connected player
	 */
	public static Player join(ScenarioContext anvil, String name, String instance) {
		SimulatedPlayer player = anvil.players().create(PlayerOptions.builder()
				.name(name + "-" + instance)
				.login(PlayerLogin.offline(name))
				.connection(PlayerConnection.builder().target(instance).build())
				.build());
		Session session = player.capability(Session.class);
		session.connect();
		session.connected();

		return new Player(player);
	}

	/**
	 * Runs a command as the player.
	 *
	 * @param command the command, without its slash
	 * @return this player
	 */
	public Player command(String command) {
		watch = messages.watch();
		messages.command(command);

		return this;
	}

	/**
	 * Waits until the player's chat shows a text, after the last command or the last text waited for.
	 *
	 * @param text the text
	 * @return this player
	 */
	public Player sees(String text) {
		watch.received(text, SOON);
		watch = messages.watch();

		return this;
	}
}
