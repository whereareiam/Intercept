package me.whereareiam.intercept.common.translation.loader;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.intercept.common.translation.loader.mapper.TranslationEntryMapper;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
import me.whereareiam.intercept.translation.namespace.NamespaceResolver;
import me.whereareiam.intercept.translation.PlatformNamespaceProvider;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Translation loader for platforms that use common message file layouts.
 */
@Singleton
public class DefaultTranslationLoader extends AbstractTranslationLoader {
	private final TranslationEntryMapper entryMapper;

	@Inject
	public DefaultTranslationLoader(
			@Named("messagesPath") Path messagesPath,
			MessageFormatRegistry formatRegistry,
			ReservedKeyRegistry reservedKeyRegistry,
			NamespaceResolver namespaceResolver,
			PlatformNamespaceProvider namespaceProvider,
			@Named("defaultLocale") Provider<Locale> defaultLocaleProvider,
			TranslationEntryMapper entryMapper
	) {
		super(messagesPath, formatRegistry, reservedKeyRegistry, namespaceResolver, namespaceProvider, defaultLocaleProvider);
		this.entryMapper = entryMapper;
	}

	@Override
	protected Map<String, TranslationEntry> loadEntries(String keyPrefix, MessageFileData data) {
		return entryMapper.mapEntries(keyPrefix, data);
	}
}
