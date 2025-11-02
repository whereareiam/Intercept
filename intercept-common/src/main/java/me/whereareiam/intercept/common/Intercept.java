package me.whereareiam.intercept.common;

import com.google.inject.Inject;
import me.whereareiam.intercept.event.EventListener;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.base.IntercepticEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptBootstrappedEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptReadyEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptStartedEvent;
import me.whereareiam.intercept.logging.Logger;

public class Intercept implements EventListener {
	private final EventManager eventManager;

	@Inject
	public Intercept(EventManager eventManager) {
		this.eventManager = eventManager;
		this.eventManager.register(this);
	}

	@IntercepticEvent
	public void onBootstrapped(InterceptBootstrappedEvent event) {
		Logger.info("Intercept has been bootstrapped - core infrastructure ready");
	}

	@IntercepticEvent
	public void onReady(InterceptReadyEvent event) {
		Logger.info("Intercept is ready - plugin fully operational");
		
		// Fire InterceptStartedEvent to signal complete startup
		eventManager.call(new InterceptStartedEvent());
	}
}
