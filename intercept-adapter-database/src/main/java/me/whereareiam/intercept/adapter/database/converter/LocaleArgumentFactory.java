package me.whereareiam.intercept.adapter.database.converter;

import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Optional;

/**
 * Argument factory for converting Locale objects to database VARCHAR strings.
 * Stores null for null locales (representing use of client locale).
 */
public class LocaleArgumentFactory implements ArgumentFactory {
	@Override
	public Optional<Argument> build(Type type, Object value, ConfigRegistry config) {
		if (type != Locale.class) return Optional.empty();

		Locale locale = (Locale) value;
		return Optional.of((position, statement, ctx) -> {
			// Null means use client locale
			if (locale == null) {
				statement.setNull(position, java.sql.Types.VARCHAR);
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

