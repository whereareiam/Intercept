package me.whereareiam.intercept.adapter.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.commandant.Commandant;
import me.whereareiam.commandant.CommandantKeys;
import me.whereareiam.intercept.adapter.command.definition.CommandDefinition;
import me.whereareiam.intercept.adapter.command.definition.CommandDefinitionAdapter;
import me.whereareiam.intercept.adapter.command.definition.Commands;
import me.whereareiam.intercept.adapter.command.executor.LanguageCommand;
import me.whereareiam.intercept.adapter.command.executor.PublishCommand;
import me.whereareiam.intercept.adapter.command.executor.ReloadCommand;
import me.whereareiam.intercept.command.CommandSource;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Registers {@code /language} and the {@code /intercept} subcommands, each under the names, permission and
 * switch {@code commands.yml} gives it.
 */
@Singleton
public final class InterceptCommands implements Lifecycle {
	private static final String ROOT = "intercept";

	private final Provider<CommandManager<CommandSource>> manager;
	private final Commands commands;
	private final LanguageCommand language;
	private final PublishCommand publish;
	private final ReloadCommand reload;
	private final CommandDefinitionAdapter adapter = new CommandDefinitionAdapter();

	@Inject
	public InterceptCommands(
			@NotNull Provider<CommandManager<CommandSource>> manager,
			@NotNull Commands commands,
			@NotNull LanguageCommand language,
			@NotNull PublishCommand publish,
			@NotNull ReloadCommand reload
	) {
		this.manager = manager;
		this.commands = commands;
		this.language = language;
		this.publish = publish;
		this.reload = reload;
	}

	@Override
	public @NotNull Phase phase() {
		return Phase.RUNTIME;
	}

	@Override
	public void start() {
		CommandManager<CommandSource> manager = this.manager.get();

		register(manager, "language", List.of(), builder -> language.build(builder));
		register(manager, "publish", List.of(ROOT), builder -> publish.build(builder));
		register(manager, "reload", List.of(ROOT), builder -> reload.build(builder));
	}

	@Override
	public void stop() {
	}

	// A subcommand of /intercept is named after its first alias; Commandant applies every alias of a root command.
	private void register(
			CommandManager<CommandSource> manager,
			String id,
			List<String> parents,
			Function<Command.Builder<CommandSource>, Command.Builder<CommandSource>> body
	) {
		CommandDefinition definition = commands.get(id);
		if (!definition.isEnabled()) return;

		String name = definition.getAliases().isEmpty() ? id : definition.getAliases().getFirst();
		Command.Builder<CommandSource> builder = parents.isEmpty()
				? manager.commandBuilder(name)
				: manager.commandBuilder(parents.getFirst()).literal(name);
		builder = builder.meta(CommandantKeys.DEFINITION_ID, id);
		if (!definition.getPermission().isEmpty()) builder = builder.permission(definition.getPermission());

		Command<CommandSource> command = body.apply(builder).build();
		if (parents.isEmpty()) {
			Commandant.process(command, manager).withDefinition(definition, adapter).register();
			return;
		}

		manager.command(command);
	}
}
