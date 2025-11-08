package me.whereareiam.intercept.platform.common.interceptor.packetevents;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.interceptor.Interceptor;
import me.whereareiam.intercept.interceptor.InterceptorProvider;
import me.whereareiam.intercept.interceptor.chat.ChatInterceptionProcessor;
import me.whereareiam.intercept.platform.common.interceptor.packetevents.chat.PacketEventsChatInterceptionProcessor;
import me.whereareiam.intercept.type.InterceptedComponentType;
import org.bukkit.Bukkit;

import java.util.EnumSet;
import java.util.Set;

/**
 * Provider for PacketEvents-based interceptors.
 * Creates interceptors that use the PacketEvents library for component interception.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PacketEventsInterceptorProvider implements InterceptorProvider {
	private static final String PLUGIN_NAME = "PacketEvents";
	private static final int PRIORITY = 100;

	private final ChatInterceptionProcessor chatInterceptionProcessor;

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
	public Set<InterceptedComponentType> getSupportedComponents() {
		return EnumSet.of(InterceptedComponentType.CHAT);
	}

	@Override
	public Interceptor createInterceptor(InterceptedComponentType type) {
		return switch (type) {
			case CHAT -> new PacketEventsChatInterceptionProcessor(chatInterceptionProcessor);
		};
	}
}

