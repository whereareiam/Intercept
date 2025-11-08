package me.whereareiam.intercept;

import me.whereareiam.intercept.type.Version;

public final class Constants {
	public static final String NAME = BuildConfig.NAME;
	public static final String VERSION = BuildConfig.VERSION;

	public static Version SERVER_VERSION = Version.UNKNOWN;

	public static final class Dependency {
		public static final String GUICE = BuildConfig.GUICE;
		public static final String CONFIGURA = BuildConfig.CONFIGURA;
		public static final String ADVENTURE = BuildConfig.ADVENTURE;
		public static final String ADVENTURE_PLATFORM_BUKKIT = BuildConfig.ADVENTURE_PLATFORM_BUKKIT;
		public static final String PACKETEVENTS = BuildConfig.PACKETEVENTS;
	}
}
