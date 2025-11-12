package me.whereareiam.intercept.common.logging;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.common.interceptor.InterceptorRegistry;
import me.whereareiam.intercept.interceptor.Interceptor;
import me.whereareiam.intercept.interceptor.InterceptorProvider;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.type.AnsiColor;
import me.whereareiam.intercept.type.ComponentType;
import me.whereareiam.intercept.type.PlatformType;
import me.whereareiam.intercept.type.PluginType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class WelcomeBannerPrinter {
	private final LoggingHelper loggingHelper;
	private final InterceptorRegistry interceptorRegistry;
	private final MessageRegistry messageRegistry;

	public void print() {
		List<String> lines = new ArrayList<>();
		lines.addAll(buildTitleLines());
		lines.addAll(buildMessagesLine());
		lines.addAll(buildAdapterLines());
		lines.add("");

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

	private List<String> buildMessagesLine() {
		List<String> l = new ArrayList<>();
		int totalKeys = messageRegistry.getKeys().size();

		l.add("  Messages: " + AnsiColor.CYAN + totalKeys + " keys" + AnsiColor.RESET);
		l.add("");

		return l;
	}

	private List<String> buildAdapterLines() {
		List<String> l = new ArrayList<>();
		Map<ComponentType, Interceptor> activeInterceptors = interceptorRegistry.getActiveInterceptors();

		if (activeInterceptors.isEmpty()) {
			l.add(AnsiColor.YELLOW + "  No interceptors active" + AnsiColor.RESET);
			return l;
		}

		// Get the provider (same for all components)
		InterceptorProvider provider = interceptorRegistry.getBestProvider(
				activeInterceptors.keySet().iterator().next()
		);

		if (provider != null) {
			l.add("  Components [" + provider.getName() + "]:");

			// List all active component types
			String components = activeInterceptors.keySet().stream()
					.map(Enum::name)
					.collect(Collectors.joining(", "));

			l.add("   - " + AnsiColor.CYAN + components + AnsiColor.RESET);
		}

		return l;
	}
}