package me.whereareiam.intercept.common.config.template;

import com.google.inject.Singleton;
import me.whereareiam.intercept.model.CommandDefinition;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.intercept.model.config.Commands;

import java.util.List;
import java.util.Map;

@Singleton
public class CommandsTemplate implements TemplateProvider<Commands> {
	@Override
	public Commands supply(Commands commands) {
		// Main command definition (root command)
		CommandDefinition main = CommandDefinition.builder()
				.enabled(true)
				.aliases(List.of("intercept"))
				.permission("")
				.description("Main command")
				.usage("{alias}")
				.cooldown(CommandDefinition.Cooldown.builder()
						.enabled(false)
						.build())
				.build();

		// Help command definition (subcommand)
		CommandDefinition help = CommandDefinition.builder()
				.enabled(true)
				.aliases(List.of("help", "h"))
				.permission("")
				.description("Help command")
				.usage("{command} {alias} [page]")
				.cooldown(CommandDefinition.Cooldown.builder()
						.enabled(false)
						.build())
				.build();

		// Reload command definition (subcommand)
		CommandDefinition reload = CommandDefinition.builder()
				.enabled(true)
				.aliases(List.of("reload", "r"))
				.permission("")
				.description("Reload the plugin configuration")
				.usage("{command} {alias}")
				.cooldown(CommandDefinition.Cooldown.builder()
						.enabled(false)
						.build())
				.build();

		// Inspect command definition (subcommand)
		CommandDefinition inspect = CommandDefinition.builder()
				.enabled(true)
				.aliases(List.of("inspect", "i"))
				.permission("")
				.description("Toggle inspection mode")
				.usage("{command} {alias}")
				.cooldown(CommandDefinition.Cooldown.builder()
						.enabled(false)
						.build())
				.build();

		// Locale self command definition (root)
		CommandDefinition localeSelf = CommandDefinition.builder()
				.enabled(true)
				.aliases(List.of("locale", "language", "lang"))
				.permission("")
				.description("Change preferred locale")
				.usage("{alias} <locale>")
				.cooldown(CommandDefinition.Cooldown.builder()
						.enabled(false)
						.build())
				.arguments(Map.of(
						"locale", "Locale"
				))
				.build();

		// Locale target command definition (root)
		CommandDefinition localeTarget = CommandDefinition.builder()
				.enabled(true)
				.aliases(List.of("locale", "language", "lang"))
				.permission("intercept.locale.target")
				.description("Change player's preferred locale")
				.usage("{command} {alias} <player> <locale>")
				.cooldown(CommandDefinition.Cooldown.builder()
						.enabled(false)
						.build())
				.arguments(Map.of(
						"player", "Recipient",
						"locale", "Locale"
				))
				.build();

		// Upload translations command definition (subcommand)
		CommandDefinition uploadTranslations = CommandDefinition.builder()
				.enabled(true)
				.aliases(List.of("database upload", "database u"))
				.permission("")
				.description("Upload translations")
				.usage("{command} {alias}")
				.cooldown(CommandDefinition.Cooldown.builder()
						.enabled(false)
						.build())
				.build();

		// Download translations command definition (subcommand)
		CommandDefinition downloadTranslations = CommandDefinition.builder()
				.enabled(true)
				.aliases(List.of("database download", "database d"))
				.permission("")
				.description("Download translations")
				.usage("{command} {alias}")
				.cooldown(CommandDefinition.Cooldown.builder()
						.enabled(false)
						.build())
				.build();

		commands.getCommands().put("main", main);
		commands.getCommands().put("help", help);
		commands.getCommands().put("reload", reload);
		commands.getCommands().put("inspect", inspect);
		commands.getCommands().put("locale-self", localeSelf);
		commands.getCommands().put("locale-target", localeTarget);
		commands.getCommands().put("database-upload", uploadTranslations);
		commands.getCommands().put("database-download", downloadTranslations);

		return commands;
	}
}
