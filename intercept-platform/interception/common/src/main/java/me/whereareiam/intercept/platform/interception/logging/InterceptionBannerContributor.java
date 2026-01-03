package me.whereareiam.intercept.platform.interception.logging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.logging.BannerContributor;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.type.AnsiColor;

import java.util.List;

@Singleton
public class InterceptionBannerContributor implements BannerContributor {
	private final InterceptionRegistry registry;

	@Inject
	public InterceptionBannerContributor(InterceptionRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void contribute(List<String> lines) {
		int keys = registry.getKeys().size();
		int patterns = registry.getAll().values().stream().mapToInt(List::size).sum();

		lines.add("  Interception: " + AnsiColor.CYAN + patterns + " patterns" + AnsiColor.RESET +
				" (" + keys + " keys)");
		lines.add("");
	}
}
