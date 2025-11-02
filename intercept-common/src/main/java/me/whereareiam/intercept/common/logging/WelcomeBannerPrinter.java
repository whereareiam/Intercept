package me.whereareiam.intercept.common.logging;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.type.AnsiColor;
import me.whereareiam.intercept.type.PlatformType;
import me.whereareiam.intercept.type.PluginType;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class WelcomeBannerPrinter {
	private final LoggingHelper loggingHelper;

	public void print() {
		List<String> lines = new ArrayList<>();
		lines.addAll(buildTitleLines());
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
}
