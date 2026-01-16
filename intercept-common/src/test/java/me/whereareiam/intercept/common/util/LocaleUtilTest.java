package me.whereareiam.intercept.common.util;

import me.whereareiam.intercept.util.LocaleUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocaleUtilTest {
	@Test
	@DisplayName("formatLocaleKey converts hyphenated tags to underscore keys")
	void formatLocaleUsesUnderscoreSeparator() {
		Locale locale = Locale.forLanguageTag("en");

		assertEquals("en", LocaleUtil.formatLocale(locale));
	}

	@Test
	@DisplayName("parseLocale accepts underscore separated strings")
	void parseLocaleSupportsUnderscoreFormat() {
		Locale locale = LocaleUtil.parseLocale("pt_BR");

		assertEquals("pt", locale.getLanguage());
		assertEquals("BR", locale.getCountry());
	}
}