package me.whereareiam.intercept.common.persistence;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.intercept.common.persistence.format.DefaultFormatContext;
import me.whereareiam.intercept.common.util.MessageFormatUtil;
import me.whereareiam.intercept.persistence.MessageFileWriter;
import me.whereareiam.intercept.persistence.format.MessageFormat;
import me.whereareiam.intercept.persistence.file.TranslationFileCodec;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecRegistry;
import me.whereareiam.intercept.persistence.file.TranslationFileCodecResolver;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
import me.whereareiam.intercept.translation.namespace.NamespaceResolver;
import me.whereareiam.intercept.model.messaging.file.MessageFileData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

@Singleton
public class DefaultTranslationFileWriter implements MessageFileWriter {
	private final MessageFormatRegistry formatRegistry;
	private final ReservedKeyRegistry reservedKeyRegistry;
	private final Provider<Locale> defaultLocaleProvider;
	private final NamespaceResolver namespaceResolver;
	private final TranslationFileCodecRegistry codecRegistry;
	private final TranslationFileCodecResolver codecResolver;

	@Inject
	public DefaultTranslationFileWriter(
			MessageFormatRegistry formatRegistry,
			ReservedKeyRegistry reservedKeyRegistry,
			NamespaceResolver namespaceResolver,
			@Named("defaultLocale") Provider<Locale> defaultLocaleProvider,
			TranslationFileCodecRegistry codecRegistry,
			TranslationFileCodecResolver codecResolver
	) {
		this.formatRegistry = formatRegistry;
		this.reservedKeyRegistry = reservedKeyRegistry;
		this.defaultLocaleProvider = defaultLocaleProvider;
		this.namespaceResolver = namespaceResolver;
		this.codecRegistry = codecRegistry;
		this.codecResolver = codecResolver;
	}

	@Override
	public void write(String relativePath, MessageFileData fileData) {
		write(relativePath, fileData, null);
	}

	@Override
	public void write(String relativePath, MessageFileData fileData, String formatId) {
		write(null, relativePath, fileData, formatId);
	}

	@Override
	public void write(String namespace, String relativePath, MessageFileData fileData, String formatId) {
		ResolvedTarget resolvedTarget = resolveTarget(namespace, relativePath, null);
		Path root = resolveNamespacePath(namespace);
		Path target = resolvedTarget.path();

		try {
			Path parent = target.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}

			MessageFormat format = resolveFormat(formatId);
			DefaultFormatContext context = new DefaultFormatContext(
					root,
					target,
					defaultLocaleProvider.get(),
					namespace,
					reservedKeyRegistry
			);
			ObjectNode data = format.write(fileData, context);
			TranslationFileCodec codec = resolvedTarget.codec();
			if (codec == null) {
				throw new IllegalStateException("No translation file codec available");
			}
			Object raw = MessageFormatUtil.toRawObject(data);
			codec.write(target, MessageFormatUtil.toStringMap(raw));
		} catch (IOException e) {
			throw new IllegalStateException("Failed to write message persistence: " + target, e);
		}
	}

	@Override
	public Path resolvePath(String relativePath) {
		return resolvePath(null, relativePath);
	}

	@Override
	public Path resolvePath(String namespace, String relativePath) {
		ResolvedTarget resolved = resolveTarget(namespace, relativePath, null);
		return resolved.path();
	}

	private MessageFormat resolveFormat(String formatId) {
		if (formatRegistry == null) throw new IllegalStateException("No message format registry available");

		if (formatId != null && !formatId.isBlank()) {
			Optional<MessageFormat> format = formatRegistry.get(formatId);
			if (format.isPresent()) return format.get();
		}

		return formatRegistry.getDefault().orElseThrow(() ->
				new IllegalStateException("No default message format registered"));
	}

	private Path resolveNamespacePath(String namespace) {
		Path root = namespaceResolver == null ? null : namespaceResolver.resolveNamespaceRoot(namespace);
		if (root == null) {
			throw new IllegalStateException("No namespace root available for message write");
		}
		return root;
	}

	private ResolvedTarget resolveTarget(String namespace, String relativePath, String fileTypeId) {
		if (relativePath == null) {
			throw new IllegalArgumentException("Relative path cannot be null");
		}
		Path base = resolveNamespacePath(namespace);
		String normalized = relativePath.replace('\\', '/');
		TranslationFileCodec codec = resolveCodec(namespace, normalized, fileTypeId);
		String extension = resolveExtension(codec);
		Path target = resolvePathWithExtension(base, normalized, extension);
		return new ResolvedTarget(codec, target.normalize());
	}

	private TranslationFileCodec resolveCodec(String namespace, String relativePath, String fileTypeId) {
		if (codecResolver != null) {
			TranslationFileCodec resolved = codecResolver.resolve(namespace, relativePath, fileTypeId);
			if (resolved != null) return resolved;
		}
		if (codecRegistry != null && fileTypeId != null && !fileTypeId.isBlank()) {
			TranslationFileCodec resolved = codecRegistry.resolveById(fileTypeId, namespace).orElse(null);
			if (resolved != null) return resolved;
		}
		return codecRegistry == null ? null : codecRegistry.getDefault();
	}

	private String resolveExtension(TranslationFileCodec codec) {
		if (codec != null && codec.getFileExtensions() != null && !codec.getFileExtensions().isEmpty()) {
			String ext = codec.getFileExtensions().get(0);
			if (ext != null && !ext.isBlank()) return ext;
		}
		return ".yml";
	}

	private Path resolvePathWithExtension(Path base, String relativePath, String extension) {
		String normalized = relativePath.replace('\\', '/');
		if (extension != null && !extension.isBlank()) {
			String lower = normalized.toLowerCase(Locale.ROOT);
			String lowerExt = extension.toLowerCase(Locale.ROOT);
			if (!lower.endsWith(lowerExt)) {
				normalized = normalized + extension;
			}
		}
		return base.resolve(normalized);
	}

	private record ResolvedTarget(TranslationFileCodec codec, Path path) {
	}
}
