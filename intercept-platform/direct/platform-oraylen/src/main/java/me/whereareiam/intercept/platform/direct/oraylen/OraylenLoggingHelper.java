package me.whereareiam.intercept.platform.direct.oraylen;

import com.google.inject.Inject;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.model.config.Settings;
import org.slf4j.Logger;

public final class OraylenLoggingHelper implements LoggingHelper {
	private final Logger logger;
	private final Settings settings;

	@Inject
	public OraylenLoggingHelper(Logger logger, Settings settings) {
		this.logger = logger;
		this.settings = settings;
	}

	@Override
	public void info(String message, Object... objects) {
		if (logger != null && level() >= 2) {
			logger.info(format(message, objects));
		}
	}

	@Override
	public void warn(String message, Object... objects) {
		if (logger != null && level() >= 1) {
			logger.warn(format(message, objects));
		}
	}

	@Override
	public void severe(String message, Object... objects) {
		if (logger != null && level() >= 0) {
			logger.error(format(message, objects));
		}
	}

	@Override
	public void debug(String message, Object... objects) {
		if (logger != null && level() >= 4) {
			logger.debug(format(message, objects));
		}
	}

	private int level() {
		return settings != null ? settings.getLevel() : 2;
	}

	private String format(String message, Object... objects) {
		if (message == null) return "null";
		return objects == null || objects.length == 0 ? message : String.format(message, objects);
	}
}
