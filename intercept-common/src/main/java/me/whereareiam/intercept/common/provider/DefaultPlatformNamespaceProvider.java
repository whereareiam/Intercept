package me.whereareiam.intercept.common.provider;

import me.whereareiam.intercept.Constants;
import me.whereareiam.intercept.translation.PlatformNamespaceProvider;

/**
 * Default platform namespace provider for interception platforms.
 */
public class DefaultPlatformNamespaceProvider implements PlatformNamespaceProvider {
	@Override
	public String getRuntimeNamespace() {
		return Constants.Namespace.INTERNAL;
	}
}
