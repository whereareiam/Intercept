package me.whereareiam.intercept.platform.direct.common.persistence.format;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.persistence.format.FormatContext;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;

import java.nio.file.Path;
import java.util.Locale;

@Getter
@RequiredArgsConstructor
public final class DirectFormatContext implements FormatContext {
	private final Path root;
	private final Path file;
	private final Locale defaultLocale;
	private final ReservedKeyRegistry reservedKeyRegistry;

	@Override
	public String getNamespace() {
		return null;
	}
}

