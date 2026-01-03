package me.whereareiam.intercept.platform.interception;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import me.whereareiam.intercept.platform.interception.config.provider.InterceptionProvider;
import me.whereareiam.intercept.platform.interception.interceptor.InterceptionLifecycleListener;
import me.whereareiam.intercept.platform.interception.interceptor.InterceptorRegistry;
import me.whereareiam.intercept.platform.interception.interceptor.InterceptorService;
import me.whereareiam.intercept.platform.interception.interceptor.processor.DefaultActionBarInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.processor.DefaultChatInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.processor.DefaultKickInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.listener.InspectionModeEnhancer;
import me.whereareiam.intercept.common.logging.BannerContributor;
import me.whereareiam.intercept.common.messaging.MessageLifecycleHook;
import me.whereareiam.intercept.common.messaging.persistence.MessageDocumentProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.actionbar.ActionBarInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.chat.ChatInterceptionProcessor;
import me.whereareiam.intercept.platform.interception.interceptor.kick.KickInterceptionProcessor;
import me.whereareiam.intercept.messaging.InterceptionRegistry;
import me.whereareiam.intercept.messaging.RegexMatchingService;
import me.whereareiam.intercept.messaging.TagReplacementService;
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
		bind(InterceptionRegistry.class).to(DefaultInterceptionRegistry.class).asEagerSingleton();

		bind(InterceptionProvider.class).asEagerSingleton();
		bind(Interception.class).toProvider(InterceptionProvider.class);

		bind(TagReplacementService.class).to(DefaultTagReplacementService.class);
		bind(RegexMatchingService.class).to(DefaultRegexMatchingService.class);

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
		Multibinder.newSetBinder(binder(), MessageDocumentProcessor.class)
				.addBinding().to(InterceptionMessageDocumentProcessor.class);

		Multibinder.newSetBinder(binder(), BannerContributor.class)
				.addBinding().to(InterceptionBannerContributor.class);
	}
}
