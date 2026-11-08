package me.whereareiam.intercept.interception.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;
import me.whereareiam.intercept.interception.Surface;
import org.jetbrains.annotations.NotNull;

/**
 * The contents of {@code interception.yml}: which messages from the server Intercept translates, and the marker
 * plugins write to ask for a key.
 */
@Getter
@Setter
@ToString
public class Interception {
	@Comment("Chat messages from the server and its plugins; signed player chat is never changed.")
	private @NotNull MessageKind chat = new MessageKind();
	@Comment("Messages above the hotbar.")
	private @NotNull MessageKind actionBar = new MessageKind();
	@Comment("The reason shown when a player is kicked or cannot join.")
	private @NotNull MessageKind kick = new MessageKind();

	/**
	 * Returns how one kind of message is translated.
	 *
	 * @param surface the kind of message
	 * @return its settings
	 */
	public @NotNull MessageKind of(@NotNull Surface surface) {
		return switch (surface) {
			case CHAT -> chat;
			case ACTION_BAR -> actionBar;
			case KICK -> kick;
		};
	}

	/**
	 * How one kind of message is translated.
	 */
	@Getter
	@Setter
	@ToString
	public static class MessageKind {
		private boolean enabled = true;
		@Comment("The marker's opening, its name and its closing, such as <lang> for <lang key=\"...\">.")
		private @NotNull String marker = "<lang>";
		@Comment("Whether the rule files in translations/rules/ and translations/local/rules/ apply.")
		private boolean rules = true;
	}
}
