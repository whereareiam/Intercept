package me.whereareiam.intercept.adapter.database.message;

import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.intercept.persistence.format.MessageFormat;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.persistence.format.ReservedKeyHandler;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
import me.whereareiam.intercept.translation.namespace.NamespaceResolver;
import me.whereareiam.intercept.translation.PlatformNamespaceProvider;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.nio.file.Path;

final class TestMessageSupport {
	private TestMessageSupport() {
	}

	static MessageFormatRegistry createFormatRegistry() {
		MessageFormatRegistry registry = new TestMessageFormatRegistry();
		registry.register(new TestMessageFormat(), true);
		return registry;
	}

	static ReservedKeyRegistry createReservedKeyRegistry() {
		return new TestReservedKeyRegistry();
	}

	static NamespaceResolver createNamespaceResolver(
			Settings settings,
			PlatformNamespaceProvider provider,
			Path messagesRoot
	) {
		return new TestNamespaceResolver(settings, provider, messagesRoot);
	}

	private static final class TestMessageFormatRegistry implements MessageFormatRegistry {
		private final Map<String, MessageFormat> formats = new HashMap<>();
		private MessageFormat defaultFormat;

		@Override
		public void register(MessageFormat format) {
			register(format, false);
		}

		@Override
		public void register(MessageFormat format, boolean setDefault) {
			if (format == null || format.getId() == null || format.getId().isBlank()) return;
			formats.put(format.getId(), format);
			if (setDefault || defaultFormat == null) {
				defaultFormat = format;
			}
		}

		@Override
		public Optional<MessageFormat> get(String id) {
			if (id == null) return Optional.empty();
			return Optional.ofNullable(formats.get(id));
		}

		@Override
		public Optional<MessageFormat> getDefault() {
			return Optional.ofNullable(defaultFormat);
		}

		@Override
		public Collection<MessageFormat> getAll() {
			return java.util.List.copyOf(formats.values());
		}
	}

	private static final class TestMessageFormat implements MessageFormat {
		@Override
		public String getId() {
			return "TEST";
		}

		@Override
		public MessageFileData parse(
				ObjectNode rawData,
				me.whereareiam.intercept.persistence.format.FormatContext context
		) {
			return new MessageFileData();
		}

		@Override
		public ObjectNode write(
				MessageFileData data,
				me.whereareiam.intercept.persistence.format.FormatContext context
		) {
			return new ObjectNode();
		}

		@Override
		public boolean canRepresent(MessageFileData.Entry entry) {
			return true;
		}
	}

	private static final class TestReservedKeyRegistry implements ReservedKeyRegistry {
		private final Map<String, ReservedKeyHandler> handlers = new HashMap<>();

		@Override
		public void register(ReservedKeyHandler handler) {
			if (handler == null || handler.getKey() == null || handler.getKey().isBlank()) return;
			handlers.put(normalize(handler.getKey()), handler);
		}

		@Override
		public void unregister(String key) {
			if (key == null || key.isBlank()) return;
			handlers.remove(normalize(key));
		}

		@Override
		public Optional<ReservedKeyHandler> get(String key) {
			if (key == null || key.isBlank()) return Optional.empty();
			return Optional.ofNullable(handlers.get(normalize(key)));
		}

		@Override
		public Set<String> getAllReservedKeys() {
			Set<String> keys = new java.util.HashSet<>(handlers.keySet());
			keys.add("interception");
			return Set.copyOf(keys);
		}

		@Override
		public boolean isReservedKey(String key) {
			if (key == null || key.isBlank()) return false;
			String normalized = normalize(key);
			if ("interception".equals(normalized)) return true;
			return handlers.containsKey(normalized);
		}

		private String normalize(String key) {
			return key.trim().toLowerCase(java.util.Locale.ROOT);
		}
	}

	private static final class TestNamespaceResolver implements NamespaceResolver {
		private final Settings settings;
		private final PlatformNamespaceProvider provider;
		private final Path messagesRoot;

		private TestNamespaceResolver(Settings settings, PlatformNamespaceProvider provider, Path messagesRoot) {
			this.settings = settings;
			this.provider = provider;
			this.messagesRoot = messagesRoot;
		}

		@Override
		public Set<String> resolveRuntimeNamespaces() {
			Settings.Translation.Namespaces namespaces = resolveSettings();

			List<String> extras = namespaces != null ? namespaces.getExtra() : List.of();
			List<String> load = namespaces != null ? namespaces.getLoad() : List.of();

			Set<String> runtime = new LinkedHashSet<>(resolvePlatformNamespaces());

			if (load != null && !load.isEmpty()) {
				runtime.addAll(filterValid(load));
			} else if (extras != null && !extras.isEmpty()) {
				runtime.addAll(filterValid(extras));
			}

			return runtime;
		}

		@Override
		public Set<String> resolveStorageNamespaces() {
			Settings.Translation.Namespaces namespaces = resolveSettings();
			List<String> extras = namespaces != null ? namespaces.getExtra() : List.of();

			Set<String> storage = new LinkedHashSet<>(resolvePlatformNamespaces());
			if (extras != null && !extras.isEmpty()) {
				storage.addAll(filterValid(extras));
			}

			return storage;
		}

		@Override
		public boolean usesNamespacedLayout() {
			Settings.Translation.Namespaces namespaces = resolveSettings();
			List<String> extras = namespaces != null ? namespaces.getExtra() : List.of();
			if (extras == null || extras.isEmpty()) return false;
			for (String extra : extras) {
				if (extra != null && !extra.isBlank()) return true;
			}
			return false;
		}

		@Override
		public Path resolveNamespaceRoot(String namespace) {
			if (messagesRoot == null) return null;

			if (provider != null) {
				Map<String, Path> custom = provider.getNamespacePaths(messagesRoot);
				if (custom != null && namespace != null && custom.containsKey(namespace)) {
					return custom.get(namespace);
				}
			}

			boolean namespacedLayout = usesNamespacedLayout();
			if (namespacedLayout && namespace != null && !namespace.isBlank()) {
				return messagesRoot.resolve(namespace);
			}

			return messagesRoot;
		}

		private Settings.Translation.Namespaces resolveSettings() {
			Settings.Translation translation = settings != null ? settings.getTranslation() : null;
			return translation != null ? translation.getNamespaces() : null;
		}

		private Set<String> resolvePlatformNamespaces() {
			if (provider == null) return Set.of(Constants.Namespace.INTERNAL);
			return filterValid(provider.getRuntimeNamespaces());
		}

		private Set<String> filterValid(List<String> values) {
			Set<String> filtered = new LinkedHashSet<>();
			for (String value : values) {
				if (value == null || value.isBlank()) continue;
				filtered.add(value);
			}
			return filtered;
		}

		private Set<String> filterValid(Set<String> values) {
			Set<String> filtered = new LinkedHashSet<>();
			if (values == null || values.isEmpty()) return filtered;
			for (String value : values) {
				if (value == null || value.isBlank()) continue;
				filtered.add(value);
			}
			return filtered;
		}
	}
}
