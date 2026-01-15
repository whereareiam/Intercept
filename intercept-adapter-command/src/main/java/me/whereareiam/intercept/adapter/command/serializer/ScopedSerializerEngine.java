package me.whereareiam.intercept.adapter.command.serializer;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import me.whereareiam.keystone.model.SerializerContent;
import me.whereareiam.keystone.model.SerializerOptions;
import me.whereareiam.keystone.serializer.SerializerEngine;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * SerializerEngine wrapper that applies a default scope when missing.
 */
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class ScopedSerializerEngine implements SerializerEngine {
	private final SerializerEngine delegate;
	private final String scope;

	@Override
	public @NotNull String serialize(@NotNull Component component) {
		return delegate.serialize(component);
	}

	@Override
	public @NotNull Component serialize(@NotNull SerializerContent content) {
		if (content.getScope() == null || content.getScope().isBlank()) {
			return delegate.serialize(SerializerContent.builder()
					.receiver(content.getReceiver())
					.scope(scope)
					.message(content.getMessage())
					.placeholders(content.getPlaceholders())
					.build());
		}

		return delegate.serialize(content);
	}

	@Override
	public @NotNull SerializerOptions.PlaceholderFormat getPlaceholderFormat() {
		return delegate.getPlaceholderFormat();
	}
}
