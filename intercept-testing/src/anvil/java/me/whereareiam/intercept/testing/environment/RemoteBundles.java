package me.whereareiam.intercept.testing.environment;

/**
 * Whether a test network downloads the remote bundles of {@link me.whereareiam.intercept.testing.fixture.Bundles},
 * and how their web server lays them out.
 */
public enum RemoteBundles {
	/**
	 * No remote bundles.
	 */
	NONE,
	/**
	 * The folder {@code semantica-cli bundle} writes: {@code manifest.json} and {@code <product>/<language>.json}.
	 */
	PATHS,
	/**
	 * A file depot's: the manifest at {@code @manifest} and each bundle at {@code sha256-<hash>/<language>.json}.
	 */
	DIGESTS
}
