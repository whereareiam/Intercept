package me.whereareiam.intercept.platform.interception.messaging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.messaging.MessageLifecycleHook;
import me.whereareiam.intercept.messaging.InterceptionRegistry;

@Singleton
public class InterceptionMessageLifecycleHook implements MessageLifecycleHook {
	private final InterceptionRegistry registry;

	@Inject
	public InterceptionMessageLifecycleHook(InterceptionRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void beforeLoad() {
		registry.clear();
	}
}
