package me.whereareiam.intercept.platform.direct.oraylen.translation.mapper;

import me.whereareiam.intercept.platform.direct.common.translation.source.DirectDefaultsProvider;
import me.whereareiam.intercept.platform.direct.common.translation.source.DirectTranslationSourceEntry;
import net.oraylen.api.translation.TranslationSource;
import net.oraylen.api.type.FileFormat;

import java.util.ArrayList;
import java.util.List;

public final class OraylenTranslationSourceMapper {
	public List<DirectTranslationSourceEntry> adapt(TranslationSource source) {
		if (source == null || source.sources() == null || source.sources().isEmpty()) return List.of();

		List<DirectTranslationSourceEntry> entries = new ArrayList<>();
		for (TranslationSource.Source entry : source.sources()) {
			if (entry == null) continue;

			String path = entry.path();
			FileFormat format = entry.format();
			String formatId = format != null ? format.name() : FileFormat.LOCALE.name();
			String fileType = entry.fileType();
			boolean multiLocale = format == FileFormat.MULTI_LOCALE;
			DirectDefaultsProvider defaultsProvider = adaptDefaults(entry.defaultsProvider());

			entries.add(new DirectTranslationSourceEntry(
					path,
					formatId,
					fileType,
					multiLocale,
					entry.optional(),
					defaultsProvider
			));
		}

		return entries;
	}

	private DirectDefaultsProvider adaptDefaults(TranslationSource.DefaultsProvider provider) {
		if (provider instanceof TranslationSource.DefaultsProvider.Direct direct)
			return direct::generate;

		return null;
	}
}
