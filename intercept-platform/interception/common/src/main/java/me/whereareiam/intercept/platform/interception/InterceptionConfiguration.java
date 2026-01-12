package me.whereareiam.intercept.platform.interception;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import me.whereareiam.intercept.common.persistence.format.type.multilocale.MultiLocaleFormat;
import me.whereareiam.intercept.common.persistence.format.type.template.TemplateFormat;
import me.whereareiam.intercept.registry.MessageFormatRegistry;
import me.whereareiam.intercept.registry.ReservedKeyRegistry;
import me.whereareiam.intercept.platform.interception.config.provider.InterceptionProvider;
import me.whereareiam.intercept.platform.interception.interceptor.InterceptionLifecycleListener;
import me.whereareiam.intercept.platform.interception.interceptor.InterceptorRegistry;
import me.whereareiam.intercept.platform.interception.interceptor.InterceptorService;
import me.whereareiam.intercept.platform.interception.interceptor.processor.DefaultActionBarInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.processor.DefaultChatInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.processor.DefaultKickInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.listener.InspectionModeEnhancer;
import me.whereareiam.intercept.logging.BannerContributor;
import me.whereareiam.intercept.common.persistence.DefaultTranslationFileWriter;
import me.whereareiam.intercept.platform.interception.messaging.persistence.InterceptionTranslationLoader;
import me.whereareiam.intercept.platform.interception.interceptor.actionbar.ActionBarInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.chat.ChatInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.kick.KickInterceptionProcessor;
import me.whereareiam.intercept.registry.InterceptionRegistry;
import me.whereareiam.intercept.translation.TranslationLoader;
import me.whereareiam.intercept.persistence.MessageFileWriter;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.platform.interception.logging.InterceptionBannerContributor;
import me.whereareiam.intercept.platform.interception.messaging.DefaultInterceptionRegistry;
import me.whereareiam.intercept.platform.interception.messaging.InterceptionMessageDocumentProcessor;
import me.whereareiam.intercept.platform.interception.messaging.format.InterceptionKeyHandler;

public class InterceptionConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		requestInjection(this);

		bind(InterceptionRegistry.class).to(DefaultInterceptionRegistry.class).asEagerSingleton();

		bind(InterceptionProvider.class).asEagerSingleton();
		bind(Interception.class).toProvider(InterceptionProvider.class);

		OptionalBinder.newOptionalBinder(binder(), TranslationLoader.class)
				.setBinding().to(InterceptionTranslationLoader.class);
		OptionalBinder.newOptionalBinder(binder(), MessageFileWriter.class)
				.setBinding().to(DefaultTranslationFileWriter.class);

		bind(InterceptionMessageDocumentProcessor.class).asEagerSingleton();

		bind(InterceptorRegistry.class).asEagerSingleton();
		bind(ChatInterceptionProcessor.class).to(DefaultChatInterceptionProcessor.class).asEagerSingleton();
		bind(ActionBarInterceptionProcessor.class).to(DefaultActionBarInterceptionProcessor.class).asEagerSingleton();
		bind(KickInterceptionProcessor.class).to(DefaultKickInterceptionProcessor.class).asEagerSingleton();
		bind(InterceptorService.class).asEagerSingleton();

		bind(InspectionModeEnhancer.class).asEagerSingleton();
		bind(InterceptionLifecycleListener.class).asEagerSingleton();
		bind(InterceptionHelperUpdater.class).asEagerSingleton();

		Multibinder.newSetBinder(binder(), BannerContributor.class)
				.addBinding().to(InterceptionBannerContributor.class);
	}

	@Inject
	void initializeMessageFormats(
			MessageFormatRegistry formatRegistry,
			ReservedKeyRegistry reservedKeyRegistry
	) {
		formatRegistry.register(new MultiLocaleFormat(), true);
		formatRegistry.register(new TemplateFormat(), false);
		reservedKeyRegistry.register(new InterceptionKeyHandler());
	}
}
