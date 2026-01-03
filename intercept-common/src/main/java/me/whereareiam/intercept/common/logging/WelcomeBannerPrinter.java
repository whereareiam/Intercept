package me.whereareiam.intercept.common.logging;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.logging.LoggingHelper;
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
	private final Set<BannerContributor> contributors;

	public void print() {
		List<String> lines = new ArrayList<>();
		lines.addAll(buildTitleLines());
		lines.addAll(buildMessagesLine());
		contributors.forEach(contributor -> contributor.contribute(lines));
		lines.add("");

		lines.forEach(loggingHelper::info);
	}

	private List<String> buildTitleLines() {
		List<String> l = new ArrayList<>();

		l.add("");
		l.add(AnsiColor.CYAN +
				"  Ы ЫЯЯ   " + AnsiColor.RESET +
				"Intercept v" + AnsiColor.GRAY +
				Constants.VERSION + AnsiColor.RESET);
		l.add(AnsiColor.CYAN +
				"  Ы ЫЬЬ   " + AnsiColor.RESET +
				"Platform: " + AnsiColor.GRAY +
				PlatformType.getType() + " [" +
				PluginType.getType() + "]" +
				AnsiColor.RESET);
		l.add("");

		return l;
	}

	private List<String> buildMessagesLine() {
		List<String> l = new ArrayList<>();
		int totalKeys = translationService.getKeys().size();

		l.add("  Messages: " + AnsiColor.CYAN + totalKeys + " keys" + AnsiColor.RESET);
		l.add("");

		return l;
	}
}
