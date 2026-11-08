package me.whereareiam.intercept.adapter.command.definition;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The contents of {@code commands.yml}: each of Intercept's commands, by name.
 */
@Getter
@Setter
@ToString
public class Commands {
	@Comment("Turn a command off, rename it with aliases, or change its permission.")
	private @NotNull Map<String, CommandDefinition> commands = defaults();

	private static Map<String, CommandDefinition> defaults() {
		Map<String, CommandDefinition> commands = new LinkedHashMap<>();
		commands.put("language", CommandDefinition.of(List.of("language", "lang"), "<language>", "", "Choose the language you see messages in"));
		commands.put("publish", CommandDefinition.of(List.of("publish"), "", "intercept.command.publish", "Send the changes in messages/ and rules/ to every server"));
		commands.put("reload", CommandDefinition.of(List.of("reload"), "", "intercept.command.reload", "Reload messages and rules"));

		return commands;
	}

	/**
	 * Returns a command's definition.
	 *
	 * @param id the command, such as {@code language}
	 * @return its definition, or a disabled one when the file does not name it
	 */
	public @NotNull CommandDefinition get(@NotNull String id) {
		CommandDefinition definition = commands.get(id);
		return definition != null ? definition : CommandDefinition.disabled();
	}
}
