package me.whereareiam.intercept.common.messaging;

import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.semantica.SemanticaLogger;

public class InterceptSemanticaLogger implements SemanticaLogger {
	@Override
	public void debug(String message, Object... args) {
		Logger.debug(message, args);
	}

	@Override
	public void info(String message, Object... args) {
		Logger.info(message, args);
	}

	@Override
	public void warn(String message, Object... args) {
		Logger.warn(message, args);
	}

	@Override
	public void error(String message, Object... args) {
		Logger.severe(message, args);
	}
}
