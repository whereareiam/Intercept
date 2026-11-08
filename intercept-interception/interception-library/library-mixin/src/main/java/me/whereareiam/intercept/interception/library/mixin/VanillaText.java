package me.whereareiam.intercept.interception.library.mixin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.NotNull;

/**
 * Converts between the game's text and Adventure's through their shared JSON form; NeoForge on Minecraft 1.21.1 has
 * no Adventure platform that converts directly.
 */
public final class VanillaText {
	private VanillaText() {
	}

	/**
	 * Converts the game's text to Adventure's.
	 *
	 * @param text       the game's text
	 * @param registries the registries the text's contents refer to
	 * @return the same text in Adventure
	 */
	public static @NotNull Component adventure(
			net.minecraft.network.chat.@NotNull Component text,
			@NotNull HolderLookup.Provider registries
	) {
		return GsonComponentSerializer.gson().deserialize(net.minecraft.network.chat.Component.Serializer.toJson(text, registries));
	}

	/**
	 * Converts Adventure's text to the game's.
	 *
	 * @param text       Adventure's text
	 * @param registries the registries the text's contents refer to
	 * @return the same text for the game
	 */
	public static net.minecraft.network.chat.@NotNull Component vanilla(
			@NotNull Component text,
			@NotNull HolderLookup.Provider registries
	) {
		net.minecraft.network.chat.Component converted = net.minecraft.network.chat.Component.Serializer.fromJson(
				GsonComponentSerializer.gson().serialize(text),
				registries
		);

		return converted == null ? net.minecraft.network.chat.Component.empty() : converted;
	}
}
