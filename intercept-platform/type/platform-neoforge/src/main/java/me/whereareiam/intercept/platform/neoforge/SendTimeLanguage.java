package me.whereareiam.intercept.platform.neoforge;

import me.whereareiam.intercept.language.LanguageApplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * On NeoForge every outgoing message is rendered when it is sent, in the language the player chose at that moment,
 * so a new choice applies to the next message without telling the game anything.
 */
public final class SendTimeLanguage implements LanguageApplier {
	@Override
	public void apply(@NotNull UUID uniqueId, @Nullable Locale language) {
	}
}
