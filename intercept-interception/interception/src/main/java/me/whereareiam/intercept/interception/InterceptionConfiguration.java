package me.whereareiam.intercept.interception;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.configura.Configura;
import me.whereareiam.intercept.interception.common.DefaultMessageInterceptor;
import me.whereareiam.intercept.interception.config.Interception;

import java.nio.file.Path;

/**
 * Binds the interceptor. A platform adds the packet library that feeds it.
 */
public final class InterceptionConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(MessageInterceptor.class).to(DefaultMessageInterceptor.class);
	}

	@Provides
	@Singleton
	Interception interception(Configura configura, @Named("dataPath") Path dataPath) {
		return configura.update(dataPath.resolve("interception.yml"), Interception.class);
	}
}
