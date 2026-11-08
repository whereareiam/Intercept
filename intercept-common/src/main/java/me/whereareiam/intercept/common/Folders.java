package me.whereareiam.intercept.common;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Set;

/**
 * Where Intercept keeps its translations and its cache inside its data folder:
 *
 * <pre>
 * translations/
 *   en/ de/ all/      the network's messages, a folder per language and all/ for every language
 *   rules/            the network's rule files
 *   local/            this server's own messages and rules, never published
 *     en/ de/ all/
 *     rules/
 * .cache/             written by Intercept only
 * </pre>
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class Folders {
	/**
	 * The folder of rule files, in {@code translations/} and in {@code translations/local/}.
	 */
	public static final String RULES = "rules";
	/**
	 * The folder of this server's own messages and rules, in {@code translations/}.
	 */
	public static final String LOCAL = "local";
	/**
	 * The folders inside {@code translations/} that hold no language.
	 */
	public static final Set<String> NOT_LANGUAGES = Set.of(RULES, LOCAL);

	private final @Named("dataPath") @NotNull Path dataPath;

	/**
	 * Returns the network's messages, edited on any server and published.
	 *
	 * @return {@code translations/}
	 */
	public @NotNull Path translations() {
		return dataPath.resolve("translations");
	}

	/**
	 * Returns the network's rule files.
	 *
	 * @return {@code translations/rules/}
	 */
	public @NotNull Path rules() {
		return translations().resolve(RULES);
	}

	/**
	 * Returns this server's own messages.
	 *
	 * @return {@code translations/local/}
	 */
	public @NotNull Path local() {
		return translations().resolve(LOCAL);
	}

	/**
	 * Returns this server's own rule files.
	 *
	 * @return {@code translations/local/rules/}
	 */
	public @NotNull Path localRules() {
		return local().resolve(RULES);
	}

	/**
	 * Returns a folder of Intercept's cache.
	 *
	 * @param name the folder, such as {@code remote}
	 * @return {@code .cache/<name>/}
	 */
	public @NotNull Path cache(@NotNull String name) {
		return dataPath.resolve(".cache").resolve(name);
	}
}
