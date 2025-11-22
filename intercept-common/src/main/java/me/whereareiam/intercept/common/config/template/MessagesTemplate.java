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
		messages.setPrefix("<aqua>ɪɴᴛᴇʀᴄᴇᴘᴛ <dark_gray>| ");

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
				"<aqua><bold> Intercept</bold> <white>Command help",
				" ",
				"{commands}",
				"{pagination}"
		));
		helpMessages.setCommandFormat(" <yellow>/{command}{arguments}</yellow> <dark_gray>- <white>{description}");
		helpMessages.setNoCommands("  <red>No commands found</red>");
		helpMessages.setCommandsPerPage(7);

		// Configure argument formatting
		HelpMessages.Format argumentFormat = new HelpMessages.Format();
		argumentFormat.setArgument("<gray>[{argument}]</gray>");
		argumentFormat.setOptionalArgument("<gray>({argument})</gray>");
		helpMessages.setArgumentFormat(argumentFormat);

		commands.setHelp(helpMessages);

		// Configure reload command messages
		Messages.Commands.Reload reload = new Messages.Commands.Reload();
		reload.setSuccess("{prefix}<white>Configuration reloaded <green>successfully</green>!");
		reload.setError("{prefix}<white>An <red>error occurred</red> while reloading: <gray>{error}</gray>");
		commands.setReload(reload);

		// Configure inspect command messages
		Messages.Commands.Inspect inspect = new Messages.Commands.Inspect();
		inspect.setEnabled("{prefix}<white>Inspection mode <green>enabled</green>. Click on chat messages to get regex patterns.");
		inspect.setDisabled("{prefix}<white>Inspection mode <red>disabled</red>.");

		Messages.Commands.Inspect.Hover hover = new Messages.Commands.Inspect.Hover();
		hover.setFormat(List.of(
				"",
				"<white> Regex Pattern:</white>   ",
				"<aqua>  {pattern}{truncated}</aqua>   ",
				"",
				"<green>Click to copy pattern!</green>   "
		));
		hover.setTruncationFormat("...");
		hover.setMaxPatternLength(60);
		inspect.setHover(hover);

		commands.setInspect(inspect);

		// Configure database command messages
		Messages.Commands.Database database = new Messages.Commands.Database();

		// Configure database upload command messages
		Messages.Commands.Database.Upload upload = new Messages.Commands.Database.Upload();
		upload.setNoMessages("{prefix}<white>No messages to upload.");
		upload.setUploading("{prefix}<white>Uploading <yellow>{entries}</yellow> entries from <yellow>{files}</yellow> files...");
		upload.setSuccess("{prefix}<white>Successfully uploaded <green>{entries}</green> message entries to the database.");
		upload.setError("{prefix}<white>An <red>error occurred</red> while uploading: <gray>{error}</gray>");
		database.setUpload(upload);

		commands.setDatabase(database);

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