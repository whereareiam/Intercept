package me.whereareiam.intercept.platform.interception;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import me.whereareiam.configura.Config;
import me.whereareiam.intercept.model.messaging.document.MessageDocument;
import me.whereareiam.intercept.platform.interception.config.provider.InterceptionProvider;
import me.whereareiam.intercept.common.messaging.persistence.MessageDocumentNodeAdapter;
import me.whereareiam.intercept.platform.interception.interceptor.InterceptionLifecycleListener;
import me.whereareiam.intercept.platform.interception.interceptor.InterceptorRegistry;
import me.whereareiam.intercept.platform.interception.interceptor.InterceptorService;
import me.whereareiam.intercept.platform.interception.interceptor.processor.DefaultActionBarInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.processor.DefaultChatInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.processor.DefaultKickInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.listener.InspectionModeEnhancer;
import me.whereareiam.intercept.common.logging.BannerContributor;
import me.whereareiam.intercept.common.messaging.MessageLifecycleHook;
import me.whereareiam.intercept.common.messaging.persistence.DefaultMessageFileLoader;
import me.whereareiam.intercept.common.messaging.persistence.DefaultMessageFileWriter;
import me.whereareiam.intercept.common.messaging.persistence.DefaultTranslationLoader;
import me.whereareiam.intercept.platform.interception.interceptor.actionbar.ActionBarInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.chat.ChatInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.kick.KickInterceptionProcessor;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.messaging.RegexMatchingService;
import me.whereareiam.intercept.messaging.TagReplacementService;
import me.whereareiam.intercept.messaging.TranslationLoader;
import me.whereareiam.intercept.messaging.file.MessageFileLoader;
import me.whereareiam.intercept.messaging.file.MessageFileWriter;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.platform.interception.logging.InterceptionBannerContributor;
import me.whereareiam.intercept.platform.interception.messaging.DefaultInterceptionRegistry;
import me.whereareiam.intercept.platform.interception.messaging.InterceptionMessageDocumentProcessor;
import me.whereareiam.intercept.platform.interception.messaging.InterceptionMessageLifecycleHook;
import me.whereareiam.intercept.platform.interception.regex.DefaultRegexMatchingService;
import me.whereareiam.intercept.platform.interception.tag.DefaultTagReplacementService;

public class InterceptionConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		requestInjection(this);

		bind(InterceptionRegistry.class).to(DefaultInterceptionRegistry.class).asEagerSingleton();

		bind(InterceptionProvider.class).asEagerSingleton();
		bind(Interception.class).toProvider(InterceptionProvider.class);

		OptionalBinder.newOptionalBinder(binder(), TranslationLoader.class)
				.setBinding().to(DefaultTranslationLoader.class);
		OptionalBinder.newOptionalBinder(binder(), MessageFileLoader.class)
				.setBinding().to(DefaultMessageFileLoader.class);
		OptionalBinder.newOptionalBinder(binder(), MessageFileWriter.class)
				.setBinding().to(DefaultMessageFileWriter.class);

		bind(TagReplacementService.class).to(DefaultTagReplacementService.class);
		bind(RegexMatchingService.class).to(DefaultRegexMatchingService.class);
		bind(InterceptionMessageDocumentProcessor.class).asEagerSingleton();

		bind(InterceptorRegistry.class).asEagerSingleton();
		bind(ChatInterceptionProcessor.class).to(DefaultChatInterceptionProcessor.class).asEagerSingleton();
		bind(ActionBarInterceptionProcessor.class).to(DefaultActionBarInterceptionProcessor.class).asEagerSingleton();
		bind(KickInterceptionProcessor.class).to(DefaultKickInterceptionProcessor.class).asEagerSingleton();
		bind(InterceptorService.class).asEagerSingleton();

		bind(InspectionModeEnhancer.class).asEagerSingleton();
		bind(InterceptionLifecycleListener.class).asEagerSingleton();
		bind(InterceptionHelperUpdater.class).asEagerSingleton();

		Multibinder.newSetBinder(binder(), MessageLifecycleHook.class)
				.addBinding().to(InterceptionMessageLifecycleHook.class);

		Multibinder.newSetBinder(binder(), BannerContributor.class)
				.addBinding().to(InterceptionBannerContributor.class);
	}

	@Inject
	void initializeConfiguraAdapter() {
		Config.registerAdapter(MessageDocument.Node.class, new MessageDocumentNodeAdapter());
	}
}
