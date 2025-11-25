package me.whereareiam.intercept.adapter.database.converter;

import me.whereareiam.intercept.util.LocaleUtil;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Column mapper for converting database VARCHAR locale strings to Locale objects.
 * Handles empty string "" for single-language entries and regular locale strings.
 */
public class LocaleColumnMapper implements ColumnMapper<Locale> {
	@Override
	public Locale map(ResultSet rs, int columnNumber, StatementContext ctx) throws SQLException {
		String localeString = rs.getString(columnNumber);

		if (localeString == null || localeString.isEmpty()) return null;

		// Handle special "default" locale
		if ("default".equals(localeString))
			return new Locale.Builder().setLanguage("default").build();

		return LocaleUtil.parseLocale(localeString);
	}
}



