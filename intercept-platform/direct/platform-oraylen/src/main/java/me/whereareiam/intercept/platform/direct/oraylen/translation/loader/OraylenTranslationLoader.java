package me.whereareiam.intercept.platform.direct.oraylen.translation.loader;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import me.whereareiam.intercept.platform.direct.oraylen.translation.mapper.OraylenTranslationSourceMapper;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.platform.direct.common.translation.loader.DirectTranslationLoader;
import me.whereareiam.intercept.platform.direct.common.translation.source.DirectTranslationSourceEntry;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecResolver;
import net.oraylen.api.translation.TranslationSource;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class OraylenTranslationLoader {
	private final DirectTranslationLoader loader;
	private final OraylenTranslationSourceMapper sourceAdapter;

	@Inject
	public OraylenTranslationLoader(
			@Named("defaultLocale") Provider<Locale> defaultLocaleProvider,
			MessageFormatRegistry formatRegistry,
			ReservedKeyRegistry reservedKeyRegistry,
			TranslationFileCodecRegistry codecRegistry,
			TranslationFileCodecResolver codecResolver
	) {
		this.loader = new DirectTranslationLoader(
				defaultLocaleProvider,
				formatRegistry,
				reservedKeyRegistry,
				codecRegistry,
				codecResolver,
				null
		);
		this.sourceAdapter = new OraylenTranslationSourceMapper();
	}

	public List<MessageFileData> load(Path baseDirectory, TranslationSource source) {
		return load(null, baseDirectory, source);
	}

	public List<MessageFileData> load(String namespace, Path baseDirectory, TranslationSource source) {
		List<DirectTranslationSourceEntry> entries = sourceAdapter.adapt(source);
		return loader.load(namespace, baseDirectory, entries);
	}
}
