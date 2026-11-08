package me.whereareiam.intercept.interception.library.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.language.LanguageApplier;
import me.whereareiam.intercept.language.LanguageService;
import me.whereareiam.semantica.format.adventure.MinecraftLocales;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers the language each client reports and, on a platform that rewrites it, tells the server the player's
 * chosen language instead. When a player changes their choice, their last settings are replayed to the server with
 * the new language, so the server's own view of the player's locale follows at once.
 *
 * <p>The language service is looked up lazily: it applies choices through this class.</p>
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class ClientLocales implements LanguageApplier {
	private final @NotNull Provider<LanguageService> languages;
	private final @Named("rewriteClientLocale") boolean rewrite;
	private final Map<UUID, Reported> reported = new ConcurrentHashMap<>();

	/**
	 * Returns the language a player's client reported.
	 *
	 * @param player the player, or {@code null} before the connection has a profile
	 * @return the language, English until the client reported one
	 */
	public @NotNull Locale reported(@Nullable UUID player) {
		Reported known = player == null ? null : reported.get(player);
		return known == null ? Locale.ENGLISH : MinecraftLocales.toLocale(known.code());
	}

	/**
	 * Records the settings a client sent and returns the locale the server should see.
	 *
	 * @param player   the player
	 * @param settings the settings of the play phase, kept to replay them later
	 * @return the locale code to pass on
	 */
	@NotNull String report(@NotNull UUID player, @NotNull WrapperPlayClientSettings settings) {
		String code = settings.getLocale();
		reported.put(player, new Reported(code, settings));

		return seen(player, code);
	}

	/**
	 * Records the locale a client sent during configuration and returns the locale the server should see.
	 *
	 * @param player the player
	 * @param code   the client's locale code
	 * @return the locale code to pass on
	 */
	@NotNull String report(@NotNull UUID player, @NotNull String code) {
		reported.merge(player, new Reported(code, null), (known, next) -> new Reported(code, known.settings()));
		return seen(player, code);
	}

	/**
	 * Forgets a player who disconnected.
	 *
	 * @param player the player
	 */
	void forget(@NotNull UUID player) {
		reported.remove(player);
	}

	@Override
	public void apply(@NotNull UUID player, @Nullable Locale language) {
		if (!rewrite) return;

		Reported known = reported.get(player);
		if (known == null || known.settings() == null) return;

		User user = user(player).orElse(null);
		if (user == null || user.getConnectionState() != ConnectionState.PLAY) return;

		WrapperPlayClientSettings replay = known.settings();
		replay.setLocale(language == null ? known.code() : MinecraftLocales.toCode(language));
		PacketEvents.getAPI().getProtocolManager().receivePacketSilently(user.getChannel(), replay);
	}

	private String seen(UUID player, String code) {
		if (!rewrite) return code;
		return languages.get().chosen(player).map(MinecraftLocales::toCode).orElse(code);
	}

	private static Optional<User> user(UUID player) {
		return PacketEvents.getAPI().getProtocolManager().getUsers().stream()
				.filter(user -> player.equals(user.getUUID()))
				.findFirst();
	}

	private record Reported(@NotNull String code, @Nullable WrapperPlayClientSettings settings) {
	}
}
