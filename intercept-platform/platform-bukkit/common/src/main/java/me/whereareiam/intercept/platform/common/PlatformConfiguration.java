package me.whereareiam.intercept.platform.common;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.logging.LoggingHelper;

@RequiredArgsConstructor
public class PlatformConfiguration extends AbstractModule {
	@Override
	protected void configure() {
		bind(LoggingHelper.class).to(BukkitLoggingHelper.class);
	}
}
