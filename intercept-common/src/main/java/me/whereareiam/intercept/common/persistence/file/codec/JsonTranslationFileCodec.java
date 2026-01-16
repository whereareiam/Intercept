package me.whereareiam.intercept.common.persistence.file.codec;

import me.whereareiam.configura.Config;
import me.whereareiam.configura.node.Node;
import me.whereareiam.configura.node.ObjectNode;
import me.whereareiam.configura.reader.ConfigReader;
import me.whereareiam.configura.type.Format;
import me.whereareiam.configura.writer.ConfigWriter;
import me.whereareiam.intercept.common.util.MessageFormatUtil;
import me.whereareiam.intercept.persistence.file.TranslationFileCodec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class JsonTranslationFileCodec implements TranslationFileCodec {
	private volatile ConfigReader baseReader;
	private volatile ConfigWriter baseWriter;
	private volatile ConfigReader jsonReader;
	private volatile ConfigWriter jsonWriter;

	@Override
	public String getId() {
		return "JSON";
	}

	@Override
	public List<String> getFileExtensions() {
		return List.of(Format.JSON.getExtension());
	}

	@Override
	public Map<String, Object> read(Path path) throws IOException {
		try {
			return toMap(reader().readNode(path));
		} catch (Exception e) {
			throw new IOException("Failed to read JSON file: " + path, e);
		}
	}

	@Override
	public void write(Path path, Map<String, Object> data) throws IOException {
		try {
			writer().writeNode(path, toObjectNode(data));
		} catch (Exception e) {
			throw new IOException("Failed to write JSON file: " + path, e);
		}
	}

	private ConfigReader reader() {
		ConfigReader current = Config.getDefaultReader();
		if (current != baseReader || jsonReader == null) {
			baseReader = current;
			jsonReader = current.withFormat(Format.JSON);
		}

		return jsonReader;
	}

	private ConfigWriter writer() {
		ConfigWriter current = Config.getDefaultWriter();
		if (current != baseWriter || jsonWriter == null) {
			baseWriter = current;
			jsonWriter = current.withFormat(Format.JSON);
		}

		return jsonWriter;
	}

	private Map<String, Object> toMap(Node node) {
		if (node == null) return Map.of();

		Object raw = MessageFormatUtil.toRawObject(node);
		if (raw instanceof Map<?, ?> map)
			return MessageFormatUtil.toStringMap(map);

		return Map.of();
	}

	private ObjectNode toObjectNode(Map<String, Object> data) {
		Node node = MessageFormatUtil.toNode(data);
		if (node instanceof ObjectNode objectNode)
			return objectNode;

		return new ObjectNode();
	}
}
