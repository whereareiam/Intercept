package me.whereareiam.intercept.testing.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.intercept.testing.environment.Containers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * The remote bundles of a test network: the product {@code test} with {@code remote.welcome} in English and
 * German, and the manifest that lists them with their hashes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bundles {
	/**
	 * The lines of {@code settings.yml} that point an instance at the bundles' web server.
	 */
	public static final String SETTINGS = """
			remote:
			  enabled: true
			  url: "http://${anvil.process.%s.host}:${anvil.process.%s.port}/"
			  product: test
			""".formatted(Containers.BUNDLES, Containers.BUNDLES);

	private static final Map<String, String> BUNDLES = new TreeMap<>(Map.of(
			"en", "{\"remote.welcome\": \"Hello from the bundle, {player}!\"}",
			"de", "{\"remote.welcome\": \"Hallo aus dem Bundle, {player}!\"}"
	));

	/**
	 * Returns the files the web server serves, by their path.
	 *
	 * @return {@code manifest.json} and {@code test/<language>.json}
	 */
	public static Map<String, String> files() {
		String hashes = BUNDLES.entrySet().stream()
				.map(bundle -> "\"" + bundle.getKey() + "\": \"" + sha256(bundle.getValue()) + "\"")
				.collect(Collectors.joining(", "));

		Map<String, String> files = new LinkedHashMap<>();
		files.put("manifest.json", "{\"format\": 1, \"revision\": \"1\", \"defaultLocale\": \"en\", \"bundles\": {\"test\": {" + hashes + "}}}");
		BUNDLES.forEach((language, bundle) -> files.put("test/" + language + ".json", bundle));

		return files;
	}

	private static String sha256(String contents) {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(contents.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException(exception);
		}
	}
}
