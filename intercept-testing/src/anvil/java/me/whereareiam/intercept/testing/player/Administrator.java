package me.whereareiam.intercept.testing.player;

import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.anvil.capability.dispatch.Dispatch;
import me.whereareiam.intercept.testing.environment.Platform;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * What an administrator does on one instance of a test network: edits Intercept's files, publishes them and runs
 * console commands, and has a plugin without translations send a player a system message.
 *
 * <pre>{@code
 * Administrator.on(anvil, InterceptNetwork.A)
 *         .write(Path.of("translations", "en", "test.yml"), "news: \"Fresh news\"\n")
 *         .publish();
 * }</pre>
 */
public final class Administrator {
	private final ScenarioContext anvil;
	private final String instance;
	private final Platform platform;

	private Administrator(ScenarioContext anvil, String instance) {
		this.anvil = anvil;
		this.instance = instance;
		this.platform = Platform.of(anvil);
	}

	/**
	 * Acts on one instance.
	 *
	 * @param anvil    the network
	 * @param instance the server or proxy
	 * @return the administrator
	 */
	public static Administrator on(ScenarioContext anvil, String instance) {
		return new Administrator(anvil, instance);
	}

	/**
	 * Writes a file into Intercept's folder while the instance runs.
	 *
	 * @param file     the file, relative to Intercept's folder, such as {@code translations/en/test.yml}
	 * @param contents what it holds
	 * @return this administrator
	 */
	public Administrator write(Path file, String contents) {
		Path target = anvil.processes().get(instance).workDirectory().resolve(platform.getFolder()).resolve(file);
		try {
			Files.createDirectories(target.getParent());
			Files.writeString(target, contents);
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}

		return this;
	}

	/**
	 * Sends the changes in {@code translations/} to every instance.
	 *
	 * @return this administrator
	 */
	public Administrator publish() {
		return console("intercept publish");
	}

	/**
	 * Runs a command on the instance's console.
	 *
	 * @param command the command, without its slash
	 * @return this administrator
	 */
	public Administrator console(String command) {
		run(instance, command);
		return this;
	}

	/**
	 * Sends a player a system message, as a plugin without translations would: from the instance itself, or on
	 * Velocity from the server behind the proxy.
	 *
	 * @param player the receiver's name
	 * @param text   the message's text
	 * @return this administrator
	 */
	public Administrator tell(String player, String text) {
		run(platform.sender(instance), "tellraw " + player + " \"" + text + "\"");
		return this;
	}

	private void run(String process, String command) {
		anvil.processes().get(process).capability(Dispatch.class).execute(command);
	}
}
