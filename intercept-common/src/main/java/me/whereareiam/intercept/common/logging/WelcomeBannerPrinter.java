package me.whereareiam.intercept.common.logging;

import com.google.inject.Inject;
import com.google.inject.Provider;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.logging.BannerContributor;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.model.config.Commands;
import me.whereareiam.intercept.type.AnsiColor;
import me.whereareiam.intercept.type.PlatformType;
import me.whereareiam.intercept.type.PluginType;
import me.whereareiam.semantica.translation.TranslationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class WelcomeBannerPrinter {
	private final LoggingHelper loggingHelper;
	private final TranslationService<Locale> translationService;
	private final Provider<Commands> commandsProvider;
	private final Set<BannerContributor> contributors;

	public void print() {
		List<String> lines = new ArrayList<>();
		lines.addAll(buildTitleLines());
		lines.addAll(buildSummaryLines());
		contributors.forEach(contributor -> contributor.contribute(lines));

		lines.forEach(loggingHelper::info);
	}

	private List<String> buildTitleLines() {
		List<String> l = new ArrayList<>();

		l.add("");
		l.add(AnsiColor.CYAN +
				"  █ █▀▀   " + AnsiColor.RESET +
				"Intercept v" + AnsiColor.GRAY +
				Constants.VERSION + AnsiColor.RESET);
		l.add(AnsiColor.CYAN +
				"  █ █▄▄   " + AnsiColor.RESET +
				"Platform: " + AnsiColor.GRAY +
				PlatformType.getType() + " [" +
				PluginType.getType() + "]" +
				AnsiColor.RESET);
		l.add("");

		return l;
	}

	private List<String> buildSummaryLines() {
		List<String> l = new ArrayList<>();
		int commandCount = 0;
		Commands commands = commandsProvider.get();
		if (commands != null && commands.getCommands() != null)
			commandCount = commands.getCommands().size();

		int totalKeys = translationService.getKeys().size();

		l.add("  Loaded " + AnsiColor.CYAN + commandCount + AnsiColor.RESET + " " +
				pluralize("command", commandCount));
		l.add("  Loaded " + AnsiColor.CYAN + totalKeys + AnsiColor.RESET + " " +
				pluralize("message", totalKeys));
		l.add("");

		return l;
	}

	private String pluralize(String word, int count) {
		return count == 1 ? word : word + "s";
	}
}
