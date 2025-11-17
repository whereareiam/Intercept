package me.whereareiam.intercept;

import me.whereareiam.intercept.type.Version;

public final class Constants {
	public static final String NAME = BuildConfig.NAME;
	public static final String VERSION = BuildConfig.VERSION;

	public static Version SERVER_VERSION = Version.UNKNOWN;

	public static final class Dependency {
		public static final String GUICE = BuildConfig.GUICE;
		public static final String CONFIGURA = BuildConfig.CONFIGURA;
		public static final String KEYSTONE = BuildConfig.KEYSTONE;
		public static final String COMMANDANT = BuildConfig.COMMANDANT;
		public static final String ADVENTURE = BuildConfig.ADVENTURE;
		public static final String ADVENTURE_PLATFORM_BUKKIT = BuildConfig.ADVENTURE_PLATFORM_BUKKIT;
		public static final String CLOUD_CORE = BuildConfig.CLOUD_CORE;
		public static final String CLOUD_COOLDOWN = BuildConfig.CLOUD_COOLDOWN;
		public static final String CLOUD_PAPER = BuildConfig.CLOUD_PAPER;
		public static final String CLOUD_MINECRAFT_EXTRAS = BuildConfig.CLOUD_MINECRAFT_EXTRAS;

		// DatabaseConfig
		public static final String EBEAN = BuildConfig.EBEAN;
		public static final String HIKARICP = BuildConfig.HIKARICP;
		public static final String POSTGRESQL = BuildConfig.POSTGRESQL;
		public static final String MARIADB = BuildConfig.MARIADB;
	}
}
