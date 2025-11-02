package me.whereareiam.intercept;

import me.whereareiam.intercept.type.Version;

/**
 * Interface for platform-specific interactions in the Intercept plugin system.
 * Provides methods for common server operations like broadcasting messages,
 * checking player permissions, and retrieving server information.
 *
 * <p>This interface abstracts platform-specific implementations (e.g., Bukkit, Velocity)
 * to ensure consistent behavior across different server platforms.</p>
 */
public interface PlatformInteractor {
	/**
	 * Gets the current server version.
	 *
	 * @return The server version information
	 */
	Version getServerVersion();
}