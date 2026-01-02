package me.whereareiam.intercept.common.messaging.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.messaging.file.MessageFileWriter;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Singleton
public class DefaultMessageFileWriter implements MessageFileWriter {
	private final Path messagesPath;

	@Inject
	public DefaultMessageFileWriter(@Named("messagesPath") Path messagesPath) {
		this.messagesPath = messagesPath;
	}

	@Override
	public void write(String relativePath, MessageDocument fileData) {
		Path target = resolvePath(relativePath);

		try {
			Path parent = target.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Config.save(target, fileData != null ? fileData.toMap() : Map.of());
		} catch (IOException e) {
			throw new IllegalStateException("Failed to write message persistence: " + target, e);
		}
	}

	@Override
	public Path resolvePath(String relativePath) {
		String normalized = relativePath.replace('\\', '/');
		Format format = Config.getDefaultWriter().getFormat();
		String extension = format == Format.JSON ? ".json" : ".yml";
		return messagesPath.resolve(normalized + extension).normalize();
	}
}
