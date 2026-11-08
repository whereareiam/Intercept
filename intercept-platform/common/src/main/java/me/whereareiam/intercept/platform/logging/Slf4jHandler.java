package me.whereareiam.intercept.platform.logging;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Passes Intercept's log records to an SLF4J logger, such as the one Velocity gives the plugin or NeoForge's, so they appear under Intercept's name.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Slf4jHandler extends Handler {
	private final Logger logger;

	/**
	 * Creates a logger whose records go to Velocity's.
	 *
	 * @param logger the plugin's logger
	 * @return a logger for Intercept's parts
	 */
	public static @NotNull java.util.logging.Logger logger(@NotNull Logger logger) {
		java.util.logging.Logger bridge = java.util.logging.Logger.getAnonymousLogger();
		bridge.setUseParentHandlers(false);
		bridge.addHandler(new Slf4jHandler(logger));

		return bridge;
	}

	@Override
	public void publish(LogRecord record) {
		String message = record.getMessage();
		Throwable thrown = record.getThrown();
		int level = record.getLevel().intValue();

		if (level >= Level.SEVERE.intValue()) {
			logger.error(message, thrown);
			return;
		}

		if (level >= Level.WARNING.intValue()) {
			logger.warn(message, thrown);
			return;
		}

		if (level >= Level.INFO.intValue()) {
			logger.info(message, thrown);
			return;
		}

		logger.debug(message, thrown);
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}
}
