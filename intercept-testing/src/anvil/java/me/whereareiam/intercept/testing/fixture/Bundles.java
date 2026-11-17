package me.whereareiam.intercept.testing.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.intercept.testing.environment.Containers;
import me.whereareiam.intercept.testing.environment.RemoteBundles;

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
 * German, and the manifest that lists them with their hashes, in either layout of {@link RemoteBundles}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bundles {
	private static final String SETTINGS = """
			remote:
			  enabled: true
			  url: "http://${anvil.process.%s.host}:${anvil.process.%s.port}/"
			  layout: %s
			  product: test
			""";
	private static final String DEPOT_MANIFEST = "@manifest";

	private static final Map<String, String> BUNDLES = new TreeMap<>(Map.of(
			"en", "{\"remote.welcome\": \"Hello from the bundle, {player}!\"}",
			"de", "{\"remote.welcome\": \"Hallo aus dem Bundle, {player}!\"}"
	));

	/**
	 * Returns the lines of {@code settings.yml} that point an instance at the bundles' web server.
	 *
	 * @param layout how the web server lays the files out
	 * @return the lines, empty for {@link RemoteBundles#NONE}
	 */
	public static String settings(RemoteBundles layout) {
		if (layout == RemoteBundles.NONE) return "";
		return SETTINGS.formatted(Containers.BUNDLES, Containers.BUNDLES, layout.name());
	}

	/**
	 * Returns the files the web server serves, by their path.
	 *
	 * @param layout how the files lie
	 * @return {@code manifest.json} and {@code test/<language>.json} for {@link RemoteBundles#PATHS};
	 * {@code @manifest} and {@code sha256-<hash>/<language>.json} for {@link RemoteBundles#DIGESTS}
	 * @throws IllegalArgumentException for {@link RemoteBundles#NONE}
	 */
	public static Map<String, String> files(RemoteBundles layout) {
		String hashes = BUNDLES.entrySet().stream()
				.map(bundle -> "\"" + bundle.getKey() + "\": \"" + sha256(bundle.getValue()) + "\"")
				.collect(Collectors.joining(", "));
		String manifest = "{\"format\": 1, \"revision\": \"1\", \"defaultLocale\": \"en\", \"bundles\": {\"test\": {" + hashes + "}}}";

		Map<String, String> files = new LinkedHashMap<>();
		switch (layout) {
			case PATHS -> {
				files.put("manifest.json", manifest);
				BUNDLES.forEach((language, bundle) -> files.put("test/" + language + ".json", bundle));
			}
			case DIGESTS -> {
				files.put(DEPOT_MANIFEST, manifest);
				BUNDLES.forEach((language, bundle) -> files.put("sha256-" + sha256(bundle) + "/" + language + ".json", bundle));
			}
			case NONE -> throw new IllegalArgumentException("A network without remote bundles serves no files");
		}

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
