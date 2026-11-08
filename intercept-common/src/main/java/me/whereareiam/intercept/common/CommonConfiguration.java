package me.whereareiam.intercept.common;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import me.whereareiam.configura.Configura;
import me.whereareiam.configura.feature.comment.CommentFeature;
import me.whereareiam.configura.type.Format;
import me.whereareiam.intercept.common.config.SettingsProvider;
import me.whereareiam.intercept.common.language.DefaultLanguageService;
import me.whereareiam.intercept.common.message.DefaultMessageService;
import me.whereareiam.intercept.common.replication.LocalReplicationService;
import me.whereareiam.intercept.common.translation.DefaultTranslationService;
import me.whereareiam.intercept.language.LanguageService;
import me.whereareiam.intercept.lifecycle.Lifecycle;
import me.whereareiam.intercept.message.MessageService;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.replication.ReplicationService;
import me.whereareiam.intercept.translation.TranslationService;

/**
 * Binds what every platform shares: settings, the catalog, shared messages and languages. Platforms add their own
 * configuration; persistence and replication add theirs.
 */
public final class CommonConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(Settings.class).toProvider(SettingsProvider.class);
		bind(TranslationService.class).to(DefaultTranslationService.class);
		bind(MessageService.class).to(DefaultMessageService.class);
		bind(LanguageService.class).to(DefaultLanguageService.class);

		OptionalBinder.newOptionalBinder(binder(), ReplicationService.class)
				.setDefault()
				.to(LocalReplicationService.class);

		Multibinder<Lifecycle> lifecycles = Multibinder.newSetBinder(binder(), Lifecycle.class);
		lifecycles.addBinding().to(DefaultTranslationService.class);
		lifecycles.addBinding().to(DefaultMessageService.class);
		lifecycles.addBinding().to(DefaultLanguageService.class);
	}

	@Provides
	@Singleton
	Configura configura() {
		return Configura.builder()
				.format(Format.YAML)
				.feature(CommentFeature.defaults())
				.build();
	}
}
