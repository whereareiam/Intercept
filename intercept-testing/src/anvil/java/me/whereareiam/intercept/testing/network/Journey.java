package me.whereareiam.intercept.testing.network;

import me.whereareiam.anvil.api.model.player.PlayerConnection;
import me.whereareiam.anvil.api.model.player.PlayerLogin;
import me.whereareiam.anvil.api.model.player.PlayerOptions;
import me.whereareiam.anvil.api.player.SimulatedPlayer;
import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.anvil.capability.dispatch.Dispatch;
import me.whereareiam.anvil.capability.messages.MessageWatch;
import me.whereareiam.anvil.capability.messages.Messages;
import me.whereareiam.anvil.capability.session.Session;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * One player of a journey, connected to one instance, and what the journeys do around them.
 *
 * <pre>{@code
 * Journey alice = Journey.join(anvil, "Alice", "paper-a");
 * alice.command("language de").sees("Deine Sprache ist jetzt Deutsch.");
 * }</pre>
 */
public final class Journey {
	public static final Duration SOON = Duration.ofSeconds(30);

	private final Messages messages;
	private MessageWatch watch;

	private Journey(SimulatedPlayer player) {
		this.messages = player.capability(Messages.class);
		this.watch = messages.watch();
	}

	/**
	 * Connects a player to one instance. The same name on two instances is the same player, connected twice.
	 *
	 * @param anvil    the scenario
	 * @param name     the player's name
	 * @param instance the server or proxy to connect to
	 * @return the connected player
	 */
	public static Journey join(ScenarioContext anvil, String name, String instance) {
		SimulatedPlayer player = anvil.players().create(PlayerOptions.builder()
				.name(name + "-" + instance)
				.login(PlayerLogin.offline(name))
				.connection(PlayerConnection.builder().target(instance).build())
				.build());
		Session session = player.capability(Session.class);
		session.connect();
		session.connected();

		return new Journey(player);
	}

	/**
	 * Runs a command as the player.
	 *
	 * @param command the command, without its slash
	 * @return this player
	 */
	public Journey command(String command) {
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
	public Journey sees(String text) {
		watch.received(text, SOON);
		watch = messages.watch();

		return this;
	}

	/**
	 * Sends a system message to a player from a server's console, as a plugin that is not translated would.
	 *
	 * @param anvil  the scenario
	 * @param server the server
	 * @param player the receiver
	 * @param text   the message's text
	 */
	public static void tell(ScenarioContext anvil, String server, String player, String text) {
		run(anvil, server, "tellraw " + player + " \"" + text + "\"");
	}

	/**
	 * Runs a console command.
	 *
	 * @param anvil   the scenario
	 * @param process the server or proxy
	 * @param command the command, without its slash
	 */
	public static void run(ScenarioContext anvil, String process, String command) {
		anvil.processes().get(process).capability(Dispatch.class).execute(command);
	}

	/**
	 * Writes a file into an instance's folder while it runs, as an administrator edits one.
	 *
	 * @param anvil    the scenario
	 * @param process  the server or proxy
	 * @param file     the file, relative to the process's folder
	 * @param contents what it holds
	 */
	public static void write(ScenarioContext anvil, String process, Path file, String contents) {
		Path target = anvil.processes().get(process).workDirectory().resolve(file);
		try {
			Files.createDirectories(target.getParent());
			Files.writeString(target, contents);
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}
}
