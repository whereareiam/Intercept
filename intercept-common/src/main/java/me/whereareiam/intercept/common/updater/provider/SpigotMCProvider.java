package me.whereareiam.intercept.common.updater.provider;

import com.google.inject.Singleton;
import me.whereareiam.intercept.model.update.UpdateSource;
import me.whereareiam.intercept.updater.UpdateProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Singleton
public class SpigotMCProvider implements UpdateProvider {
	private static final String UPDATE_URL = "https://api.spigotmc.org/legacy/update.php?resource=";

	@Override
	public Optional<String> fetchLatest(UpdateSource source) throws IOException {
		URL url = new URL(UPDATE_URL + source.getId());
		try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
			String version = in.readLine();
			return Optional.ofNullable(version);
		}
	}

	@Override
	public List<String> fetchRecentUpdates(UpdateSource source, int limit) throws IOException {
		return fetchLatest(source)
				.map(List::of)
				.orElse(List.of());
	}
}
