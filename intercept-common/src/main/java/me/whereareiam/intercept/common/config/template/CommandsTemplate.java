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

		commands.getCommands().put("main", main);
		commands.getCommands().put("help", help);

		return commands;
	}
}

