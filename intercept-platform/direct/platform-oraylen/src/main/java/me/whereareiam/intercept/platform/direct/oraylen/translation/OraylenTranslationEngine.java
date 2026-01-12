package me.whereareiam.intercept.platform.direct.oraylen.translation;

import com.google.inject.Inject;
import com.google.inject.Provider;
import me.whereareiam.intercept.platform.direct.oraylen.translation.loader.mapper.OraylenTranslationEntryMapper;
import me.whereareiam.intercept.translation.mapper.PlaceholderMapper;
import me.whereareiam.intercept.platform.direct.oraylen.translation.loader.OraylenTranslationLoader;
import me.whereareiam.keystone.serializer.SerializerEngine;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.semantica.translation.base.TranslationLocale;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;
import net.kyori.adventure.text.Component;
import net.oraylen.api.Namespace;
import net.oraylen.api.translation.Placeholder;
import net.oraylen.api.translation.TranslationContext;
import net.oraylen.api.translation.TranslationEngine;
import net.oraylen.api.translation.TranslationSource;

import com.google.inject.name.Named;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class OraylenTranslationEngine implements TranslationEngine {
	private final SerializerEngine serializerEngine;
	private final Provider<Locale> defaultLocaleProvider;
	private final TranslationService<Locale> translationService;
	private final OraylenTranslationRegistry registry;
	private final OraylenTranslationLoader loader;
	private final OraylenTranslationEntryMapper entryMapper;
	private final PlaceholderMapper<Map<String, Placeholder>> placeholderMapper;

	@Inject
	public OraylenTranslationEngine(
			SerializerEngine serializerEngine,
			@Named("defaultLocale") Provider<Locale> defaultLocaleProvider,
			TranslationService<Locale> translationService,
			OraylenTranslationRegistry registry,
			OraylenTranslationLoader loader,
			OraylenTranslationEntryMapper entryMapper,
			PlaceholderMapper<Map<String, Placeholder>> placeholderMapper
	) {
		this.serializerEngine = serializerEngine;
		this.defaultLocaleProvider = defaultLocaleProvider;
		this.translationService = translationService;
		this.registry = registry;
		this.loader = loader;
		this.entryMapper = entryMapper;
		this.placeholderMapper = placeholderMapper;
	}

	@Override
	public Component translate(String key, Locale locale, TranslationContext context) {
		if (key == null) return Component.empty();

		Locale effectiveLocale = locale != null ? locale : defaultLocaleProvider.get();

		Map<String, Object> placeholders = placeholderMapper.map(context != null
				? context.placeholders()
				: null);

		String resolved = translationService.resolve(key, effectiveLocale, placeholders);
		return resolved != null ? serializerEngine.serialize(resolved) : Component.text(key);
	}

	@Override
	public synchronized void register(Namespace namespace, Path baseDirectory, TranslationSource source) {
		if (namespace == null || baseDirectory == null || source == null) return;

		OraylenTranslationRegistry.NamespaceRegistration existing = registry.getNamespaceRegistration(namespace);
		if (existing != null) {
			unregister(namespace);
		}

		List<MessageFileData> documents = loader.load(baseDirectory, source);
		Map<String, TranslationEntry> allEntries = new HashMap<>();

		for (MessageFileData document : documents) {
			Map<String, TranslationEntry> entries = entryMapper.map(namespace.value(), document);
			allEntries.putAll(entries);
		}

		if (!allEntries.isEmpty()) {
			translationService.register(allEntries);
		}

		registry.registerNamespace(namespace, baseDirectory, source, allEntries.keySet());
	}

	@Override
	public synchronized void unregister(Namespace namespace) {
		registry.unregisterNamespace(namespace);
	}

	@Override
	public boolean exists(String key) {
		return translationService.exists(key);
	}

	@Override
	public Set<Locale> availableLocales(String key) {
		Set<TranslationLocale> locales = translationService.getAvailableLocales(key);
		if (locales == null || locales.isEmpty()) return Set.of();

		return locales.stream()
				.map(locale -> ((SemanticLocale) locale).unwrap())
				.collect(Collectors.toSet());
	}

	@Override
	public synchronized void reload() {
		for (Namespace namespace : registry.getRegisteredNamespaces())
			reload(namespace);
	}

	@Override
	public synchronized void reload(Namespace namespace) {
		OraylenTranslationRegistry.NamespaceRegistration registration = registry.getNamespaceRegistration(namespace);
		if (registration == null) return;
		register(namespace, registration.baseDirectory(), registration.source());
	}

}
