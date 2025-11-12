package me.whereareiam.intercept.platform.common.interceptor.packetevents;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.interceptor.Interceptor;
import me.whereareiam.intercept.interceptor.InterceptorProvider;
import me.whereareiam.intercept.interceptor.actionbar.ActionBarInterceptionProcessor;
import me.whereareiam.intercept.interceptor.chat.ChatInterceptionProcessor;
import me.whereareiam.intercept.platform.common.interceptor.packetevents.actionbar.PacketEventsActionBarInterceptionProcessor;
import me.whereareiam.intercept.platform.common.interceptor.packetevents.chat.PacketEventsChatInterceptionProcessor;
import me.whereareiam.intercept.type.ComponentType;
import org.bukkit.Bukkit;

import java.util.EnumSet;
import java.util.Set;

/**
 * Provider for PacketEvents-based interceptors.
 * Creates interceptors that use the PacketEvents library for component interception.
 * <p>
 * Uses a centralized packet router pattern to avoid duplicate packet registrations
 * and ensure efficient packet processing across multiple component types.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PacketEventsInterceptorProvider implements InterceptorProvider {
	private static final String PLUGIN_NAME = "PacketEvents";
	private static final int PRIORITY = 100;

	private final ChatInterceptionProcessor chatInterceptionProcessor;
	private final ActionBarInterceptionProcessor actionBarInterceptionProcessor;

	private PacketEventsPacketRouter router;

	@Override
	public String getName() {
		return PLUGIN_NAME;
	}

	@Override
	public int getPriority() {
		return PRIORITY;
	}

	@Override
	public boolean isAvailable() {
		// Check if PacketEvents plugin is loaded and enabled
		return Bukkit.getPluginManager().getPlugin(PLUGIN_NAME.toLowerCase()) != null
				&& Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME.toLowerCase());
	}

	@Override
	public Set<ComponentType> getSupportedComponents() {
		return EnumSet.of(ComponentType.CHAT, ComponentType.ACTION_BAR);
	}

	@Override
	public Interceptor createInterceptor(ComponentType type) {
		if (router == null) router = new PacketEventsPacketRouter();

		// Create the appropriate processor
		// Each processor declares its own packet handlers via PacketProcessor interface
		// Router will auto-register the listener when needed
		Interceptor interceptor = switch (type) {
			case CHAT -> new PacketEventsChatInterceptionProcessor(chatInterceptionProcessor);
			case ACTION_BAR -> new PacketEventsActionBarInterceptionProcessor(actionBarInterceptionProcessor);
			case UNKNOWN -> throw new IllegalArgumentException("Cannot create interceptor for UNKNOWN component type");
		};

		PacketProcessor packetProcessor = (PacketProcessor) interceptor;
		router.registerProcessor(packetProcessor);

		return interceptor;
	}
}

