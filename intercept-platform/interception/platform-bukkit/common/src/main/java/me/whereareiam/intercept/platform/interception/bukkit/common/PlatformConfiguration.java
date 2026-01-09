package me.whereareiam.intercept.platform.interception.bukkit.common;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.name.Names;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.platform.interception.bukkit.common.config.BukkitDefaultLocaleProvider;
import me.whereareiam.intercept.platform.interception.bukkit.common.config.PlatformSettings;
import me.whereareiam.intercept.platform.interception.bukkit.common.config.provider.PlatformSettingsProvider;
import me.whereareiam.intercept.platform.interception.bukkit.common.provider.BukkitSerializerEngineProvider;
import me.whereareiam.keystone.serializer.SerializerEngine;

import java.util.Locale;

@RequiredArgsConstructor
public class PlatformConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(LoggingHelper.class).to(BukkitLoggingHelper.class);
		
		// Bind platform settings
		bind(PlatformSettingsProvider.class).asEagerSingleton();
		bind(PlatformSettings.class).toProvider(PlatformSettingsProvider.class);
		
		OptionalBinder.newOptionalBinder(
				binder(),
				Key.get(Locale.class, Names.named("defaultLocale"))
		).setBinding().toProvider(BukkitDefaultLocaleProvider.class);
		bind(SerializerEngine.class).toProvider(BukkitSerializerEngineProvider.class);
	}
}
