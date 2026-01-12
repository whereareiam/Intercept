package me.whereareiam.intercept.common.persistence;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.common.persistence.format.DefaultFormatContext;
import me.whereareiam.intercept.persistence.MessageFileWriter;
import me.whereareiam.intercept.persistence.format.MessageFormat;
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

	@Inject
	public DefaultTranslationFileWriter(
			MessageFormatRegistry formatRegistry,
			ReservedKeyRegistry reservedKeyRegistry,
			NamespaceResolver namespaceResolver,
			@Named("defaultLocale") Provider<Locale> defaultLocaleProvider
	) {
		this.formatRegistry = formatRegistry;
		this.reservedKeyRegistry = reservedKeyRegistry;
		this.defaultLocaleProvider = defaultLocaleProvider;
		this.namespaceResolver = namespaceResolver;
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
		Path root = resolveNamespacePath(namespace);
		Path target = resolvePath(namespace, relativePath);

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
			Config.getDefaultWriter().writeNode(target, data);
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
		String normalized = relativePath.replace('\\', '/');
		Format format = Config.getDefaultWriter().getFormat();

		Path base = resolveNamespacePath(namespace);
		return base.resolve(normalized + format.getExtension()).normalize();
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
}
