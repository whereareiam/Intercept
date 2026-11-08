package me.whereareiam.intercept.adapter.command.definition;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * One command as {@code commands.yml} defines it. Subcommands of {@code /intercept} take their first alias as the
 * subcommand's name.
 */
@Getter
@Setter
@ToString
public class CommandDefinition {
	private boolean enabled = true;
	private @NotNull List<String> aliases = new ArrayList<>();
	private @NotNull String permission = "";
	private @NotNull String description = "";
	/**
	 * The arguments after the command's name, such as {@code <language>}; Commandant builds them from this.
	 */
	private @NotNull String usage = "";

	static CommandDefinition of(List<String> aliases, String usage, String permission, String description) {
		CommandDefinition definition = new CommandDefinition();
		definition.setAliases(new ArrayList<>(aliases));
		definition.setUsage(usage);
		definition.setPermission(permission);
		definition.setDescription(description);

		return definition;
	}

	static CommandDefinition disabled() {
		CommandDefinition definition = new CommandDefinition();
		definition.setEnabled(false);

		return definition;
	}
}
