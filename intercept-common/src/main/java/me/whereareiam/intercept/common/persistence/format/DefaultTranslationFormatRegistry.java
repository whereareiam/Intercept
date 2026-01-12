package me.whereareiam.intercept.common.persistence.format;

import com.google.inject.Singleton;
import me.whereareiam.intercept.persistence.format.MessageFormat;
import me.whereareiam.intercept.registry.MessageFormatRegistry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Default format registry implementation.
 */
@Singleton
public class DefaultTranslationFormatRegistry implements MessageFormatRegistry {
	private final Map<String, MessageFormat> formats = new LinkedHashMap<>();
	private String defaultId;

	@Override
	public void register(MessageFormat format) {
		register(format, false);
	}

	@Override
	public void register(MessageFormat format, boolean setDefault) {
		if (format == null || format.getId() == null || format.getId().isBlank())
			return;

		String id = normalizeId(format.getId());
		formats.put(id, format);
		if (setDefault || defaultId == null)
			defaultId = id;
	}

	@Override
	public Optional<MessageFormat> get(String id) {
		if (id == null || id.isBlank()) return Optional.empty();
		return Optional.ofNullable(formats.get(normalizeId(id)));
	}

	@Override
	public Optional<MessageFormat> getDefault() {
		return defaultId == null
				? Optional.empty()
				: Optional.ofNullable(formats.get(defaultId));
	}

	@Override
	public Collection<MessageFormat> getAll() {
		return formats.values();
	}

	private String normalizeId(String id) {
		return id.trim().toUpperCase(Locale.ROOT);
	}
}
