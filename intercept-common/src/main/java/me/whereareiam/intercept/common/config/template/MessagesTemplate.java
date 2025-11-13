package me.whereareiam.intercept.common.config.template;

import com.google.inject.Singleton;
import me.whereareiam.commandant.model.message.ExceptionMessages;
import me.whereareiam.commandant.model.message.HelpMessages;
import me.whereareiam.commandant.model.message.PaginationMessages;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.type.ComponentType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class MessagesTemplate implements TemplateProvider<Messages> {
	@Override
	public Messages supply(Messages messages) {
		// Set default prefix
		messages.setPrefix("<gold>ɪɴᴛᴇʀᴄᴇᴘᴛ <dark_gray>| ");

		// Configure command messages
		Messages.Commands commands = new Messages.Commands();

		// Configure command exception messages
		ExceptionMessages exceptionMessages = new ExceptionMessages();
		exceptionMessages.setNoPermission("{prefix}<white>You don't have \"<gray>{content}</gray>\" permission to use this command.</white>");
		exceptionMessages.setExecutionError("{prefix}<white>An error occurred while executing the command:</white> <gray>{content}</gray>");
		exceptionMessages.setInvalidSyntax("{prefix}<white>Invalid syntax, please use:</white> <yellow>/{content}</yellow>");
		exceptionMessages.setInvalidSyntaxBoolean("{prefix}<white>You tried to use <gray>{content}</gray> as a boolean, but it's not a valid value, please use <green>true</green> or <red>false</red>.</white>");
		exceptionMessages.setInvalidSyntaxNumber("{prefix}<white>You tried to use <gray>{content}</gray> as a number, but it's not a valid value, please use a valid number.</white>");
		exceptionMessages.setInvalidSyntaxString("{prefix}<white>You tried to use <gray>{content}</gray> as a string, but it's not a valid value, please use a valid string.</white>");
		exceptionMessages.setInvalidSender("{prefix}<white>You cannot execute this command from this context.</white>");

		commands.setExceptions(exceptionMessages);

		// Configure pagination messages
		PaginationMessages paginationMessages = new PaginationMessages();
		paginationMessages.setShowPaginationIfOnePage(false);
		paginationMessages.setFormat("\n {previous}<white>Pagination</white> <gray>[{current}/{max}]</gray>{next} \n");
		paginationMessages.setShowPreviousEvenIfFirst(false);
		paginationMessages.setPreviousTagFormat("<red><click:run_command:/intercept help {previousPage}>«</red> ");
		paginationMessages.setShowNextEvenIfLast(false);
		paginationMessages.setNextTagFormat(" <green><click:run_command:/intercept help {nextPage}>»</green>");

		commands.setPagination(paginationMessages);

		// Configure help messages
		HelpMessages helpMessages = new HelpMessages();
		helpMessages.setFormat(List.of(
				" ",
				"<gold><bold> Intercept</bold> <white>Command help",
				" ",
				"{commands}",
				"{pagination}"
		));
		helpMessages.setCommandFormat(" <yellow>/{command}{arguments}</yellow> <dark_gray>- <white>{description}");
		helpMessages.setNoCommands("  <red>No commands found</red>");

		// Configure argument formatting
		HelpMessages.Format argumentFormat = new HelpMessages.Format();
		argumentFormat.setArgument("<gray>[{argument}]</gray>");
		argumentFormat.setOptionalArgument("<gray>({argument})</gray>");
		helpMessages.setArgumentFormat(argumentFormat);

		commands.setHelp(helpMessages);

		// Configure custom argument names
		commands.setArguments(Map.of(
				"page", "page"
		));

		messages.setCommands(commands);

		// Configure fallback behavior
		Messages.Fallback fallback = new Messages.Fallback();
		fallback.setEnabled(true);
		fallback.setWarnAdmins(true);

		// Default fallback format - just show the key
		Messages.Fallback.SourceFormat defaultFormat = new Messages.Fallback.SourceFormat();
		defaultFormat.setEnabled(true);
		defaultFormat.setFormat("<dark_gray>{key}</dark_gray>");
		defaultFormat.setLogMissing(true);
		fallback.setDefaultFormat(defaultFormat);

		// Source-specific formats
		Map<ComponentType, Messages.Fallback.SourceFormat> formats = new HashMap<>();

		// CHAT - Simple bracket notation for missing chat messages
		Messages.Fallback.SourceFormat chatFormat = new Messages.Fallback.SourceFormat();
		chatFormat.setEnabled(true);
		chatFormat.setFormat("<dark_gray>{key}</dark_gray>");
		chatFormat.setLogMissing(true);
		formats.put(ComponentType.CHAT, chatFormat);

		fallback.setFormats(formats);
		messages.setFallback(fallback);

		return messages;
	}
}