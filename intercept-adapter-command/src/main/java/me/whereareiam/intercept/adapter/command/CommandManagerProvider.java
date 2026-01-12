package me.whereareiam.intercept.adapter.command;

import com.google.inject.Provider;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.type.PlatformType;
import me.whereareiam.intercept.type.Version;
import me.whereareiam.keystone.Actor;
import org.incendo.cloud.CommandManager;

/**
 * Abstract provider for creating platform-specific CommandManager instances.
 * Handles version detection and switches between legacy and modern command managers.
 */
@RequiredArgsConstructor
public abstract class CommandManagerProvider implements Provider<CommandManager<Actor>> {
	protected final Provider<Settings> settings;

	private CommandManager<Actor> commandManager;

	@Override
	public CommandManager<Actor> get() {
		if (commandManager != null) return commandManager;

		commandManager = switch (PlatformType.getType()) {
			case BUKKIT, SPIGOT -> createLegacyCommandManager();
			case FOLIA, PAPER -> {
				if (Constants.SERVER_VERSION.isAtLeast(Version.V_1_20_5) && settings.get().getCommands().isUseBrigadier())
					yield createPaperCommandManager();

				yield createLegacyCommandManager();
			}
			case VELOCITY -> createVelocityCommandManager();
			default -> throw new IllegalStateException("Unknown platform type");
		};

		return commandManager;
	}

	/**
	 * Creates a legacy Paper command manager using CommandSender mapping.
	 *
	 * @return The legacy command manager instance
	 */
	protected abstract CommandManager<Actor> createLegacyCommandManager();

	/**
	 * Creates a modern Paper command manager using CommandSourceStack (Brigadier).
	 *
	 * @return The modern command manager instance
	 */
	protected abstract CommandManager<Actor> createPaperCommandManager();

	/**
	 * Creates a Velocity command manager.
	 *
	 * @return The velocity command manager instance
	 */
	protected abstract CommandManager<Actor> createVelocityCommandManager();
}
