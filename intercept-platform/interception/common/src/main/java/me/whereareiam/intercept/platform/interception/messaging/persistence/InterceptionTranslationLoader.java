package me.whereareiam.intercept.platform.interception.messaging.persistence;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.intercept.common.translation.loader.AbstractTranslationLoader;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecResolver;
import me.whereareiam.intercept.translation.namespace.NamespaceResolver;
import me.whereareiam.intercept.translation.PlatformNamespaceProvider;
import me.whereareiam.intercept.common.translation.loader.mapper.TranslationEntryMapper;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import me.whereareiam.intercept.platform.interception.messaging.InterceptionMessageDocumentProcessor;
import me.whereareiam.intercept.platform.interception.messaging.format.InterceptionKeyHandler;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Default file-based translation loader for Intercept message documents.
 */
@Singleton
public class InterceptionTranslationLoader extends AbstractTranslationLoader {
	private final TranslationEntryMapper entryMapper;
	private final InterceptionMessageDocumentProcessor documentProcessor;

	@Inject
	public InterceptionTranslationLoader(
			@Named("messagesPath") Path messagesPath,
			TranslationEntryMapper entryMapper,
			InterceptionMessageDocumentProcessor documentProcessor,
			MessageFormatRegistry formatRegistry,
			ReservedKeyRegistry reservedKeyRegistry,
			NamespaceResolver namespaceResolver,
			PlatformNamespaceProvider namespaceProvider,
			@Named("defaultLocale") Provider<Locale> defaultLocaleProvider,
			TranslationFileCodecRegistry codecRegistry,
			TranslationFileCodecResolver codecResolver
	) {
		super(messagesPath, formatRegistry, reservedKeyRegistry, namespaceResolver, namespaceProvider, defaultLocaleProvider, codecRegistry, codecResolver);
		this.entryMapper = entryMapper;
		this.documentProcessor = documentProcessor;
		reservedKeyRegistry.register(new InterceptionKeyHandler());
	}

	@Override
	protected Map<String, TranslationEntry> loadEntries(String keyPrefix, MessageFileData data) {
		return entryMapper.mapEntries(keyPrefix, data);
	}

	@Override
	protected void processDocument(String keyPrefix, MessageFileData data) {
		if (documentProcessor == null) return;
		documentProcessor.process(keyPrefix, data);
	}
}
