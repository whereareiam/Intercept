package me.whereareiam.intercept.platform.interception;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import me.whereareiam.intercept.Reloadable;
import me.whereareiam.intercept.logging.InterceptionHelper;
import me.whereareiam.intercept.model.config.Settings;
import me.whereareiam.intercept.registry.base.Registry;

@Singleton
public class InterceptionHelperUpdater implements Reloadable {
	private final Provider<Settings> settingsProvider;

	@Inject
	public InterceptionHelperUpdater(
			Provider<Settings> settingsProvider,
			Registry<Reloadable> reloadables
	) {
		this.settingsProvider = settingsProvider;
		reloadables.register(this);
		update();
	}

	@Override
	public void reload() {
		update();
	}

	private void update() {
		Settings settings = settingsProvider.get();
		boolean enabled = settings != null && settings.getLevel() > 2;
		InterceptionHelper.init(enabled);
	}
}
