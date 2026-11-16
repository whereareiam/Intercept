package me.whereareiam.intercept.common.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.InterceptAPI;
import me.whereareiam.intercept.language.LanguageService;
import org.jetbrains.annotations.NotNull;

/**
 * The API Intercept registers for other plugins.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class DefaultInterceptAPI implements InterceptAPI {
	private final @NotNull LanguageService languages;

	@Override
	public @NotNull LanguageService languages() {
		return languages;
	}
}
