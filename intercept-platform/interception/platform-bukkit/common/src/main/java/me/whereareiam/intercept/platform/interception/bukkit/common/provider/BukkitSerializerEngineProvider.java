package me.whereareiam.intercept.platform.interception.bukkit.common.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.common.SerializerDecoratorRegistrar;
import me.whereareiam.intercept.common.tag.serializer.TagProcessingDecorator;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.platform.interception.bukkit.common.config.PlatformSettings;
import me.whereareiam.intercept.registry.base.Registry;
import me.whereareiam.keystone.Serializers;
import me.whereareiam.keystone.model.SerializerOptions;
import me.whereareiam.keystone.serializer.SerializerEngine;
import org.jetbrains.annotations.NotNull;

/**
 * Guice Provider for SerializerEngine instances on Bukkit platforms.
 */
@Singleton
public class BukkitSerializerEngineProvider implements Provider<SerializerEngine>, Reloadable {
	private final Provider<Messages> messagesProvider;
	private final Provider<PlatformSettings> settingsProvider;
	private final TagProcessingDecorator tagProcessingDecorator;
	private volatile SerializerEngine engine;

	@Inject
	public BukkitSerializerEngineProvider(
			@NotNull Provider<Messages> messagesProvider,
			@NotNull Provider<PlatformSettings> settingsProvider,
			@NotNull TagProcessingDecorator tagProcessingDecorator,
			@NotNull Registry<Reloadable> reloadables
	) {
		this.messagesProvider = messagesProvider;
		this.settingsProvider = settingsProvider;
		this.tagProcessingDecorator = tagProcessingDecorator;

		reloadables.register(this);
	}

	@Override
	@NotNull
	public SerializerEngine get() {
		if (engine == null) {
			Messages messages = messagesProvider.get();
			PlatformSettings.Serialization serialization = settingsProvider.get().getSerialization();

			String adapter = serialization != null && serialization.getType() != null
					? serialization.getType()
					: "MINIMESSAGE";

			boolean enableLegacyColors = serialization != null && serialization.isEnableLegacyColors();

			SerializerOptions options = SerializerOptions.builder()
					.defaultAdapter(adapter)
					.enableLegacyColors(enableLegacyColors)
					.enablePlayerNamePlaceholder(true)
					.placeholderFormat(SerializerOptions.PlaceholderFormat.custom("<", ">"))
					.build();

			SerializerEngine newEngine = Serializers.createEngine(options);

			SerializerDecoratorRegistrar.registerDefaults(
					newEngine,
					messages::getPrefix,
					tagProcessingDecorator
			);

			engine = newEngine;
		}

		return engine;
	}

	@Override
	public void reload() {
		engine = null;
	}
}
