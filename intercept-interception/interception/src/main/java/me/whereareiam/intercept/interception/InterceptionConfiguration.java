package me.whereareiam.intercept.interception;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import me.whereareiam.intercept.interception.common.DefaultMessageInterceptor;
import me.whereareiam.intercept.interception.common.InterceptionProvider;
import me.whereareiam.intercept.interception.config.Interception;
import me.whereareiam.intercept.lifecycle.RuntimeLifecycle;

/**
 * Binds the interceptor and {@code interception.yml}. A platform adds the packet library that feeds it.
 */
public final class InterceptionConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(Interception.class).toProvider(InterceptionProvider.class);
		bind(MessageInterceptor.class).to(DefaultMessageInterceptor.class);

		Multibinder.newSetBinder(binder(), RuntimeLifecycle.class).addBinding().to(InterceptionProvider.class);
	}
}
