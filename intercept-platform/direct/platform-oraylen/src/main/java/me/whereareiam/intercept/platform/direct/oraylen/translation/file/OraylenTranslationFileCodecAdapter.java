package me.whereareiam.intercept.platform.direct.oraylen.translation.file;

import me.whereareiam.intercept.persistence.file.TranslationFileCodec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

final class OraylenTranslationFileCodecAdapter implements TranslationFileCodec {
	private final net.oraylen.api.translation.file.TranslationFileCodec delegate;

	OraylenTranslationFileCodecAdapter(net.oraylen.api.translation.file.TranslationFileCodec delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getId() {
		return delegate == null ? null : delegate.id();
	}

	@Override
	public List<String> getFileExtensions() {
		return delegate == null ? List.of() : delegate.fileExtensions();
	}

	@Override
	public Map<String, Object> read(Path path) throws IOException {
		if (delegate == null) return Map.of();
		return delegate.read(path);
	}

	@Override
	public void write(Path path, Map<String, Object> data) throws IOException {
		if (delegate == null) return;
		delegate.write(path, data);
	}
}
