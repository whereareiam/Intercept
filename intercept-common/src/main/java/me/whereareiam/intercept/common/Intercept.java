package me.whereareiam.intercept.common;

import com.google.inject.Inject;
import com.google.inject.Injector;
import me.whereareiam.intercept.CommandService;
import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.InterceptAPI;
import me.whereareiam.intercept.PlatformInteractor;
import me.whereareiam.intercept.common.interceptor.InterceptorService;
import me.whereareiam.intercept.common.logging.WelcomeBannerPrinter;
import me.whereareiam.intercept.common.messaging.MessageLifecycleService;
import me.whereareiam.intercept.common.updater.UpdateScheduler;
import me.whereareiam.intercept.event.EventListener;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.base.IntercepticEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptBootstrappedEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptReadyEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptShutdownEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptStartedEvent;
import me.whereareiam.intercept.listener.ListenerRegistrar;
import me.whereareiam.intercept.logging.Logger;
import me.whereareiam.intercept.logging.LoggingHelper;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.util.EventUtil;

public class Intercept implements EventListener {
	private final Injector injector;

	@Inject
	public Intercept(
			EventManager eventManager,
			Injector injector
	) {
		this.injector = injector;
		eventManager.register(this);
	}

	@IntercepticEvent
	public void onBootstrapped(InterceptBootstrappedEvent event) {
		Constants.SERVER_VERSION = injector.getInstance(PlatformInteractor.class).getServerVersion();
		Logger.init(injector.getInstance(LoggingHelper.class));

		// Load settings early - this will trigger @PostProcess which initializes InterceptionHelper
		injector.getInstance(Settings.class);

		// Initialize the public API for external plugins
		InterceptAPI.initialize(injector);
	}

	@IntercepticEvent
	public void onReady(InterceptReadyEvent event) {
		injector.getInstance(ListenerRegistrar.class).registerListeners();

		// Initialize messages system
		injector.getInstance(MessageLifecycleService.class).initialize();

		// Initialize interceptors (providers registered by platform classes)
		injector.getInstance(InterceptorService.class).initialize();

		// Initialize commands
		injector.getInstance(CommandService.class);

		injector.getInstance(WelcomeBannerPrinter.class).print();
		injector.getInstance(UpdateScheduler.class).start();

		// Fire InterceptStartedEvent to signal complete startup
		EventUtil.callEvent(new InterceptStartedEvent());
	}

	@IntercepticEvent
	public void onShutdown(InterceptShutdownEvent event) {
		injector.getInstance(InterceptorService.class).shutdown();

		// Shutdown the public API
		InterceptAPI.shutdown();
	}
}
