package me.whereareiam.intercept.common.updater;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import me.whereareiam.intercept.model.update.UpdateSource;
import me.whereareiam.intercept.updater.UpdateProvider;

@Singleton
public class UpdateProviderRegistry {
	private final UpdateProvider modrinth;
	private final UpdateProvider spigot;
	private final UpdateProvider github;

	@Inject
	public UpdateProviderRegistry(
			@Named("MODRINTH") UpdateProvider modrinth,
			@Named("SPIGOT") UpdateProvider spigot,
			@Named("GITHUB") UpdateProvider github
	) {
		this.modrinth = modrinth;
		this.spigot = spigot;
		this.github = github;
	}

	public UpdateProvider by(UpdateSource source) {
		return switch (source.getProvider()) {
			case MODRINTH -> modrinth;
			case SPIGOT -> spigot;
			case GITHUB -> github;
		};
	}
}
