package me.whereareiam.intercept.command.executor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.commandant.Command;
import me.whereareiam.commandant.model.CommandDefinition;
import me.whereareiam.intercept.Serializer;
import me.whereareiam.intercept.model.config.Commands;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.model.player.InterceptPlayer;
import me.whereareiam.intercept.registry.PlayerRegistry;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.Player;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Command that toggles inspection mode for a user.
 * When inspection mode is enabled, messages become clickable and show regex patterns.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class InspectCommand implements Command<Actor> {
	private static final String COMMAND_NAME = "inspect";
	private final Provider<Commands> commandsProvider;
	private final Provider<Messages> messagesProvider;
	private final PlayerRegistry playerRegistry;

	@Override
	@NotNull
	public CommandDefinition getDefinition() {
		Commands commands = commandsProvider.get();
		CommandDefinition definition = commands.getCommands().get(COMMAND_NAME);
		if (definition == null)
			return CommandDefinition.builder()
					.enabled(false)
					.build();

		return definition;
	}

	@Override
	@NotNull
	public Consumer<CommandContext<Actor>> getHandler() {
		return this::handleCommand;
	}

	private void handleCommand(@NotNull CommandContext<Actor> context) {
		Actor sender = context.sender();

		InterceptPlayer interceptPlayer = getInterceptPlayer(sender);
		if (interceptPlayer == null) return;

		boolean enabled = toggleInspectionMode(interceptPlayer);
		sendToggleResponse(sender, enabled);
	}

	/**
	 * Validates that the sender is a player and returns the InterceptPlayer instance.
	 *
	 * @param sender The command sender
	 * @return The InterceptPlayer instance, or null if validation fails
	 */
	@Nullable
	private InterceptPlayer getInterceptPlayer(@NotNull Actor sender) {
		if (!(sender instanceof Player player)) {
			sendErrorMessage(sender, "This command can only be used by players.");
			return null;
		}

		if (!(player instanceof InterceptPlayer interceptPlayer)) {
			sendErrorMessage(sender, "Unable to get player UUID.");
			return null;
		}

		return interceptPlayer;
	}

	/**
	 * Toggles inspection mode for the given player.
	 *
	 * @param interceptPlayer The player to toggle inspection mode for
	 * @return true if inspection mode is now enabled, false if disabled
	 */
	private boolean toggleInspectionMode(@NotNull InterceptPlayer interceptPlayer) {
		UUID userId = interceptPlayer.getUniqueId();

		return playerRegistry.getPlayerData(userId)
				.map(player -> {
					boolean newState = !player.isInspectionMode();
					player.setInspectionMode(newState);
					return newState;
				})
				.orElseGet(() -> {
					// If no data exists yet, sync the current player instance and enable inspection mode
					playerRegistry.syncPlayerData(interceptPlayer);
					interceptPlayer.setInspectionMode(true);
					return true;
				});
	}

	/**
	 * Sends the appropriate response message after toggling inspection mode.
	 *
	 * @param sender  The command sender
	 * @param enabled Whether inspection mode is now enabled
	 */
	private void sendToggleResponse(@NotNull Actor sender, boolean enabled) {
		Messages.Commands.Inspect inspect = messagesProvider.get().getCommands().getInspect();
		String message = enabled ? inspect.getEnabled() : inspect.getDisabled();

		Component component = Serializer.serialize(sender, message);
		sender.sendMessage(component);
	}

	/**
	 * Sends an error message to the sender.
	 *
	 * @param sender  The command sender
	 * @param message The error message (without prefix)
	 */
	private void sendErrorMessage(@NotNull Actor sender, @NotNull String message) {
		Component component = Serializer.serialize(sender,
				messagesProvider.get().getPrefix() + message);
		sender.sendMessage(component);
	}
}

