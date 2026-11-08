package me.whereareiam.intercept.integration.placeholderapi;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import me.whereareiam.intercept.lifecycle.Lifecycle;

/**
 * Adds Intercept's placeholders to PlaceholderAPI when the server runs it.
 */
public final class PlaceholderApiConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		Multibinder.newSetBinder(binder(), Lifecycle.class).addBinding().to(InterceptExpansion.class);
	}
}
