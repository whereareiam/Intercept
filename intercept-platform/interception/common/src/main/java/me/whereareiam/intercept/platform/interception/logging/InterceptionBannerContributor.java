package me.whereareiam.intercept.platform.interception.logging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.whereareiam.intercept.common.logging.BannerContributor;
import me.whereareiam.intercept.platform.interception.interceptor.InterceptorRegistry;
import me.whereareiam.intercept.platform.interception.interceptor.base.Interceptor;
import me.whereareiam.intercept.platform.interception.interceptor.base.InterceptorProvider;
import me.whereareiam.intercept.type.AnsiColor;
import me.whereareiam.intercept.type.ComponentType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class InterceptionBannerContributor implements BannerContributor {
	private final InterceptorRegistry interceptorRegistry;

	@Inject
	public InterceptionBannerContributor(InterceptorRegistry interceptorRegistry) {
		this.interceptorRegistry = interceptorRegistry;
	}

	@Override
	public void contribute(List<String> lines) {
		Map<ComponentType, Interceptor> activeInterceptors = interceptorRegistry.getActiveInterceptors();
		if (activeInterceptors.isEmpty()) {
			lines.add(AnsiColor.YELLOW + "  No interceptors active" + AnsiColor.RESET);
			lines.add("");
			return;
		}

		InterceptorProvider provider = interceptorRegistry.getBestProvider(
				activeInterceptors.keySet().iterator().next()
		);

		if (provider != null) {
			lines.add("  Components [" + provider.getName() + "]:");
		} else {
			lines.add("  Components:");
		}

		String components = activeInterceptors.keySet().stream()
				.map(Enum::name)
				.sorted()
				.collect(Collectors.joining(", "));

		lines.add("   - " + AnsiColor.CYAN + components + AnsiColor.RESET);
	}
}
