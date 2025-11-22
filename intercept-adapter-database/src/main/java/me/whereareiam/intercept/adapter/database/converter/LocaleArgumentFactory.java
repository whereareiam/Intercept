package me.whereareiam.intercept.adapter.database.converter;

import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Optional;

/**
 * Argument factory for converting Locale objects to database VARCHAR strings.
 * Handles null Locale (for single-language entries) and regular Locale objects.
 */
public class LocaleArgumentFactory implements ArgumentFactory {
	@Override
	public Optional<Argument> build(Type type, Object value, ConfigRegistry config) {
		if (type != Locale.class) return Optional.empty();

		Locale locale = (Locale) value;
		return Optional.of((position, statement, ctx) -> {
			if (locale == null) {
				// Null Locale -> empty string for single-language entries
				statement.setString(position, "");
				return;
			}

			if ("default".equals(locale.getLanguage())) {
				statement.setString(position, "default");
				return;
			}

			statement.setString(position, locale.toString());
		});
	}
}

