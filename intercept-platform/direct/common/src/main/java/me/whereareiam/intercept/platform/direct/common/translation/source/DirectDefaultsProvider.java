package me.whereareiam.intercept.platform.direct.common.translation.source;

import java.util.Locale;
import java.util.Map;

@FunctionalInterface
public interface DirectDefaultsProvider {
	Map<Locale, Map<String, Object>> generate();
}
