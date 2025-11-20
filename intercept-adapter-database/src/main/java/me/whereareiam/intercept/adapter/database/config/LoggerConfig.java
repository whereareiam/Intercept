package me.whereareiam.intercept.adapter.database.config;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import me.whereareiam.intercept.model.config.Settings;

/**
 * Configures database logger level based on application settings.
 */
public final class LoggerConfig {
	/**
	 * Configures logger level based on application settings.
	 *
	 * @param settings the application settings
	 */
	public static void configure(Settings settings) {
		Level level = mapLogLevel(settings.getLevel());
		Logger.setGlobalLogLevel(level);
	}

	/**
	 * Maps application log level to the database log level.
	 * Application levels:
	 * - 0: Severe only -> ERROR
	 * - 1: Warn+ -> WARNING
	 * - 2: Info+ -> INFO
	 * - 4+: Debug -> DEBUG
	 *
	 * @param applicationLevel the application log level
	 * @return the corresponding database log level
	 */
	private static Level mapLogLevel(int applicationLevel) {
		if (applicationLevel >= 4) return Level.DEBUG;
		if (applicationLevel == 3) return Level.INFO;
		if (applicationLevel >= 1) return Level.WARNING;

		return Level.ERROR;
	}
}

