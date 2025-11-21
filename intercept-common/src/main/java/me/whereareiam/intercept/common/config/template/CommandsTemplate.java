package me.whereareiam.intercept.common.config.template;

import com.google.inject.Singleton;
import me.whereareiam.commandant.model.CommandDefinition;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.intercept.model.config.Commands;

import java.util.List;

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
				.description("Reload all configuration and resources")
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
				.description("Toggle inspection mode to get regex patterns")
				.usage("{command} {alias}")
				.cooldown(CommandDefinition.Cooldown.builder()
						.enabled(false)
						.build())
				.build();

		// Upload translations command definition (subcommand)
		CommandDefinition uploadTranslations = CommandDefinition.builder()
				.enabled(true)
				.aliases(List.of("database upload", "database u"))
				.permission("")
				.description("Upload translations to the database")
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
				.description("Download translations from the database")
				.usage("{command} {alias}")
				.cooldown(CommandDefinition.Cooldown.builder()
						.enabled(false)
						.build())
				.build();

		commands.getCommands().put("main", main);
		commands.getCommands().put("help", help);
		commands.getCommands().put("reload", reload);
		commands.getCommands().put("inspect", inspect);
		commands.getCommands().put("database-upload", uploadTranslations);
		commands.getCommands().put("database-download", downloadTranslations);

		return commands;
	}
}

