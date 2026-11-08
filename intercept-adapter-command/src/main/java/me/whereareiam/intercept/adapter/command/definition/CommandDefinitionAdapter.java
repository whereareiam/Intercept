package me.whereareiam.intercept.adapter.command.definition;

import me.whereareiam.commandant.adapter.DefinitionAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Lets Commandant apply a definition from {@code commands.yml} to a command.
 */
public final class CommandDefinitionAdapter implements DefinitionAdapter<CommandDefinition> {
	@Override
	public boolean isEnabled(@NotNull CommandDefinition definition) {
		return definition.isEnabled();
	}

	@Override
	public @Nullable List<String> getAliases(@NotNull CommandDefinition definition) {
		return definition.getAliases().isEmpty() ? null : definition.getAliases();
	}

	@Override
	public @Nullable String getPermission(@NotNull CommandDefinition definition) {
		return definition.getPermission().isEmpty() ? null : definition.getPermission();
	}

	@Override
	public @Nullable String getDescription(@NotNull CommandDefinition definition) {
		return definition.getDescription().isEmpty() ? null : definition.getDescription();
	}

	@Override
	public @Nullable String getUsage(@NotNull CommandDefinition definition) {
		return definition.getUsage().isEmpty() ? null : definition.getUsage();
	}

	@Override
	public @Nullable Cooldown getCooldown(@NotNull CommandDefinition definition) {
		return null;
	}

	@Override
	public @Nullable Map<String, String> getArguments(@NotNull CommandDefinition definition) {
		return null;
	}
}
