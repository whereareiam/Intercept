package me.whereareiam.intercept.adapter.database.message.coordinator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.adapter.database.entity.message.MessageEntryEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageExtensionEntity;
import me.whereareiam.intercept.adapter.database.entity.message.MessageFileEntity;
import me.whereareiam.intercept.adapter.database.message.MessageExtensionCodec;
import me.whereareiam.intercept.adapter.database.message.MessageKeyResolver;
import me.whereareiam.intercept.adapter.database.repository.message.MessageEntryRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageExtensionRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageFileRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageTemplateRepository;
import me.whereareiam.intercept.adapter.database.repository.message.MessageTranslationRepository;
import me.whereareiam.intercept.persistence.format.MessageFormat;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
import me.whereareiam.intercept.translation.namespace.NamespaceResolver;
import me.whereareiam.intercept.util.NamespaceUtil;
import me.whereareiam.intercept.model.messaging.file.MapMessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensionPayload;
import me.whereareiam.intercept.model.messaging.file.MessageExtensions;
import me.whereareiam.intercept.model.messaging.snapshot.MessageSnapshot;
import me.whereareiam.intercept.type.message.MessageType;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.model.translation.entry.LocalizedEntry;
import me.whereareiam.semantica.model.translation.entry.TemplateEntry;
import me.whereareiam.semantica.model.translation.entry.TranslationEntry;
import me.whereareiam.semantica.translation.base.TranslationLocale;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
public class MessageUploadCoordinator {
	private final MessageFileRepository fileRepository;
	private final MessageEntryRepository entryRepository;
	private final MessageTranslationRepository translationRepository;
	private final MessageTemplateRepository templateRepository;
	private final MessageExtensionRepository extensionRepository;
	private final MessageFormatRegistry formatRegistry;
	private final ReservedKeyRegistry reservedKeyRegistry;
	private final NamespaceResolver namespaceResolver;

	@Inject
	public MessageUploadCoordinator(
			MessageFileRepository fileRepository,
			MessageEntryRepository entryRepository,
			MessageTranslationRepository translationRepository,
			MessageTemplateRepository templateRepository,
			MessageExtensionRepository extensionRepository,
			MessageFormatRegistry formatRegistry,
			ReservedKeyRegistry reservedKeyRegistry,
			NamespaceResolver namespaceResolver
	) {
		this.fileRepository = fileRepository;
		this.entryRepository = entryRepository;
		this.translationRepository = translationRepository;
		this.templateRepository = templateRepository;
		this.extensionRepository = extensionRepository;
		this.formatRegistry = formatRegistry;
		this.reservedKeyRegistry = reservedKeyRegistry;
		this.namespaceResolver = namespaceResolver;
	}

	public void upload(MessageSnapshot snapshot) {
		if (snapshot == null) return;

		Map<String, Path> filePaths = snapshot.getFilePaths();
		Map<String, String> fileTypes = snapshot.getFileTypes();
		Set<String> namespaces = resolveNamespaces(filePaths);

		Map<String, MessageFileEntity> existingFiles = loadExistingFiles(namespaces);
		Map<String, MessageFileEntity> fileEntities = upsertFileEntities(
				filePaths,
				fileTypes,
				existingFiles
		);

		deleteMissingFiles(filePaths, existingFiles);

		Map<Long, Set<String>> seenEntries = processEntries(
				snapshot.getEntries(),
				fileEntities,
				snapshot.getExtensions()
		);

		deleteMissingEntries(fileEntities, seenEntries);
	}

	private Map<String, MessageFileEntity> loadExistingFiles(Set<String> namespaces) {
		Map<String, MessageFileEntity> existing = new HashMap<>();
		for (String namespace : namespaces) {
			List<MessageFileEntity> files = fileRepository.findAllByNamespace(namespace);
			for (MessageFileEntity file : files) {
				existing.put(file.getKeyPrefix(), file);
			}
		}
		return existing;
	}

	private Map<String, MessageFileEntity> upsertFileEntities(
			Map<String, Path> filePaths,
			Map<String, String> fileTypes,
			Map<String, MessageFileEntity> existingFiles
	) {
		Map<String, MessageFileEntity> fileEntities = new HashMap<>();

		for (Map.Entry<String, Path> fileEntry : filePaths.entrySet()) {
			String keyPrefix = fileEntry.getKey();
			Path filePath = fileEntry.getValue();

			String namespace = NamespaceUtil.getNamespace(keyPrefix);
			String rawPrefix = NamespaceUtil.stripNamespace(keyPrefix);
			if (namespace == null || namespace.isBlank()) {
				namespace = Constants.Namespace.INTERNAL;
				keyPrefix = NamespaceUtil.qualify(namespace, rawPrefix);
			}

			MessageFileEntity existing = existingFiles.get(keyPrefix);
			String relativePathString = resolveRelativePath(filePath, namespace);
			String resolvedType = resolveFileType(keyPrefix, fileTypes);

			MessageFileEntity entity = existing != null ? existing : new MessageFileEntity();
			entity.setNamespace(namespace);
			entity.setFilePath(relativePathString);
			entity.setFileType(resolvedType);

			MessageFileEntity saved = fileRepository.save(entity);
			fileEntities.put(keyPrefix, saved);
		}

		return fileEntities;
	}

	private void deleteMissingFiles(Map<String, Path> filePaths, Map<String, MessageFileEntity> existingFiles) {
		for (MessageFileEntity file : existingFiles.values()) {
			String keyPrefix = file.getKeyPrefix();
			if (filePaths.containsKey(keyPrefix)) continue;
			if (!isKnownFormat(file.getFileType())) continue;

			fileRepository.deleteById(file.getId());
		}
	}

	private Map<Long, Set<String>> processEntries(
			Map<String, TranslationEntry> entries,
			Map<String, MessageFileEntity> fileEntities,
			Map<String, MessageExtensions> extensions
	) {
		Map<Long, Set<String>> seenEntries = new HashMap<>();
		if (entries == null || entries.isEmpty()) return seenEntries;

		for (Map.Entry<String, TranslationEntry> entry : entries.entrySet()) {
			String fullKey = entry.getKey();
			if (!NamespaceUtil.hasNamespace(fullKey))
				fullKey = NamespaceUtil.qualify(Constants.Namespace.INTERNAL, fullKey);

			TranslationEntry messageEntry = entry.getValue();
			if (messageEntry == null) continue;

			String keyPrefix = MessageKeyResolver.findKeyPrefix(fullKey, fileEntities.keySet());
			MessageFileEntity fileEntity = fileEntities.get(keyPrefix);
			if (fileEntity == null) continue;

			String entryKeyRaw = MessageKeyResolver.extractEntryKey(fullKey, keyPrefix);
			String entryKey = unescapeEntryKey(entryKeyRaw);

			MessageEntryEntity entryEntity = entryRepository
					.findByFileIdAndEntryKey(fileEntity.getId(), entryKey)
					.orElse(null);

			if (entryEntity == null) {
				entryEntity = entryRepository.save(MessageEntryEntity.builder()
						.file(fileEntity)
						.entryKey(entryKey)
						.entryKeyRaw(entryKeyRaw)
						.entryType(resolveEntryType(messageEntry))
						.build());
			} else {
				entryEntity.setEntryType(resolveEntryType(messageEntry));
				entryRepository.save(entryEntity);
			}

			seenEntries.computeIfAbsent(fileEntity.getId(), id -> new HashSet<>()).add(entryKey);

			processTranslations(entryEntity, messageEntry);
			processExtensions(entryEntity, fullKey, extensions);
		}

		return seenEntries;
	}

	private void deleteMissingEntries(
			Map<String, MessageFileEntity> fileEntities,
			Map<Long, Set<String>> seenEntries
	) {
		for (MessageFileEntity fileEntity : fileEntities.values()) {
			if (!isKnownFormat(fileEntity.getFileType())) continue;
			Set<String> keep = seenEntries.getOrDefault(fileEntity.getId(), Set.of());
			List<MessageEntryEntity> existing = entryRepository.findAllByFileId(fileEntity.getId());
			for (MessageEntryEntity entry : existing) {
				String entryKey = entry.getEntryKey();
				if (entryKey == null || keep.contains(entryKey)) continue;
				entryRepository.deleteById(entry.getId());
			}
		}
	}

	private void processTranslations(MessageEntryEntity entryEntity, TranslationEntry messageEntry) {
		if (entryEntity == null || messageEntry == null) return;

		if (messageEntry instanceof LocalizedEntry localizedEntry) {
			if (translationRepository != null) {
				translationRepository.deleteByEntryId(entryEntity.getId());
				Map<TranslationLocale, String> translations = localizedEntry.getTranslations();
				Map<Locale, String> resolved = toLocaleMap(translations);
				for (Map.Entry<Locale, String> translation : resolved.entrySet()) {
					translationRepository.insert(entryEntity.getId(), translation.getKey(), translation.getValue());
				}
			}

			if (templateRepository != null) {
				templateRepository.deleteByEntryId(entryEntity.getId());
			}

			return;
		}

		if (messageEntry instanceof TemplateEntry templateEntry) {
			if (templateRepository != null) {
				templateRepository.deleteByEntryId(entryEntity.getId());
				String text = templateEntry.getTemplate();
				if (text != null) {
					templateRepository.insert(entryEntity.getId(), text);
				}
			}

			if (translationRepository != null) {
				translationRepository.deleteByEntryId(entryEntity.getId());
			}
		}
	}

	private void processExtensions(
			MessageEntryEntity entryEntity,
			String fullKey,
			Map<String, MessageExtensions> extensionData
	) {
		if (extensionRepository == null) return;

		MessageExtensions newExtensions = resolveExtensions(fullKey, extensionData);
		Map<String, MessageExtensionPayload> newPayloads = newExtensions == null
				? Map.of()
				: newExtensions.entries();

		List<MessageExtensionEntity> existing = extensionRepository.findAllByEntryId(entryEntity.getId());
		Map<String, MessageExtensionEntity> existingById = new HashMap<>();
		for (MessageExtensionEntity entity : existing) {
			if (entity.getExtensionId() != null) {
				existingById.put(entity.getExtensionId(), entity);
			}
		}

		for (MessageExtensionEntity entity : existing) {
			String extId = entity.getExtensionId();
			if (extId == null) continue;

			MessageExtensionPayload newPayload = newPayloads.get(extId);
			if (newPayload instanceof MapMessageExtensionPayload mapPayload) {
				String payloadData = MessageExtensionCodec.encode(mapPayload.data());
				if (payloadData != null) {
					extensionRepository.update(entity.getId(), payloadData);
				}
				continue;
			}

			boolean known = reservedKeyRegistry != null && reservedKeyRegistry.isReservedKey(extId);
			if (known) {
				extensionRepository.deleteById(entity.getId());
			}
		}

		for (MessageExtensionPayload payload : newPayloads.values()) {
			if (!(payload instanceof MapMessageExtensionPayload mapPayload)) continue;
			if (existingById.containsKey(mapPayload.id())) continue;

			String payloadData = MessageExtensionCodec.encode(mapPayload.data());
			if (payloadData != null) {
				extensionRepository.insert(entryEntity.getId(), mapPayload.id(), payloadData);
			}
		}
	}

	private MessageExtensions resolveExtensions(String fullKey, Map<String, MessageExtensions> extensionData) {
		if (extensionData == null || extensionData.isEmpty()) return null;
		MessageExtensions extensions = extensionData.get(fullKey);
		if (extensions == null && NamespaceUtil.hasNamespace(fullKey))
			extensions = extensionData.get(NamespaceUtil.stripNamespace(fullKey));
		return extensions;
	}

	private Map<Locale, String> toLocaleMap(Map<TranslationLocale, String> translations) {
		Map<Locale, String> resolved = new LinkedHashMap<>();
		for (Map.Entry<TranslationLocale, String> entry : translations.entrySet()) {
			Locale locale = toJavaLocale(entry.getKey());
			resolved.put(locale, entry.getValue());
		}

		return resolved;
	}

	private Locale toJavaLocale(TranslationLocale locale) {
		if (locale == null) return Locale.getDefault();
		if (locale instanceof SemanticLocale semanticLocale) return semanticLocale.unwrap();

		Locale.Builder builder = new Locale.Builder().setLanguage(locale.getLanguage());
		String country = locale.getCountry();
		if (country != null && !country.isBlank())
			builder.setRegion(country);
		String variant = locale.getVariant();
		if (variant != null && !variant.isBlank())
			builder.setVariant(variant);

		return builder.build();
	}

	private MessageType resolveEntryType(TranslationEntry entry) {
		if (entry instanceof LocalizedEntry)
			return MessageType.MESSAGE;

		return MessageType.TEMPLATE;
	}

	private String unescapeEntryKey(String entryKey) {
		if (entryKey == null || entryKey.isEmpty()) return entryKey;
		StringBuilder builder = new StringBuilder();
		boolean escape = false;
		for (int i = 0; i < entryKey.length(); i++) {
			char c = entryKey.charAt(i);
			if (escape) {
				builder.append(c);
				escape = false;
				continue;
			}

			if (c == '\\') {
				escape = true;
				continue;
			}

			builder.append(c);
		}

		if (escape) builder.append('\\');
		return builder.toString();
	}

	private Set<String> resolveNamespaces(Map<String, Path> filePaths) {
		Set<String> namespaces = new HashSet<>();
		for (String keyPrefix : filePaths.keySet()) {
			String namespace = NamespaceUtil.getNamespace(keyPrefix);
			if (namespace != null && !namespace.isBlank())
				namespaces.add(namespace);
		}

		if (namespaces.isEmpty()) {
			namespaces.add(Constants.Namespace.INTERNAL);
		}

		return namespaces;
	}

	private String resolveRelativePath(Path filePath, String namespace) {
		if (filePath == null) return "";
		Path base = resolveNamespaceRoot(namespace);

		Path relative;
		try {
			relative = base.relativize(filePath);
		} catch (Exception e) {
			relative = filePath.getFileName();
		}

		String relativePathString = relative == null ? "" : relative.toString().replace('\\', '/');

		int lastDot = relativePathString.lastIndexOf('.');
		if (lastDot > 0) relativePathString = relativePathString.substring(0, lastDot);

		return relativePathString;
	}

	private Path resolveNamespaceRoot(String namespace) {
		return namespaceResolver == null ? null : namespaceResolver.resolveNamespaceRoot(namespace);
	}

	private String resolveFileType(String keyPrefix, Map<String, String> fileTypes) {
		String type = fileTypes == null ? null : fileTypes.get(keyPrefix);
		if (type == null && NamespaceUtil.hasNamespace(keyPrefix))
			type = fileTypes.get(NamespaceUtil.stripNamespace(keyPrefix));

		if (type == null || type.isBlank()) {
			Optional<String> defaultId = formatRegistry == null
					? Optional.empty()
					: formatRegistry.getDefault().map(MessageFormat::getId);
			return defaultId.orElse(null);
		}

		return type;
	}

	private boolean isKnownFormat(String fileType) {
		if (formatRegistry == null) return true;
		String resolved = fileType;
		if (resolved == null || resolved.isBlank()) {
			resolved = formatRegistry.getDefault().map(MessageFormat::getId).orElse(null);
		}

		return resolved != null && formatRegistry.get(resolved).isPresent();
	}
}
