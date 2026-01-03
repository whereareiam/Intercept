package me.whereareiam.intercept.platform.interception.interceptor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.event.EventListener;
import me.whereareiam.intercept.event.EventManager;
import me.whereareiam.intercept.event.base.EventOrder;
import me.whereareiam.intercept.event.base.IntercepticEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptReadyEvent;
import me.whereareiam.intercept.event.lifecycle.InterceptShutdownEvent;

@Singleton
public class InterceptionLifecycleListener implements EventListener {
	private final InterceptorService interceptorService;

	@Inject
	public InterceptionLifecycleListener(EventManager eventManager, InterceptorService interceptorService) {
		this.interceptorService = interceptorService;
		eventManager.register(this);
	}

	@IntercepticEvent(EventOrder.NORMAL)
	public void onReady(InterceptReadyEvent event) {
		interceptorService.initialize();
	}

	@IntercepticEvent(EventOrder.LOW)
	public void onShutdown(InterceptShutdownEvent event) {
		interceptorService.shutdown();
	}
}
