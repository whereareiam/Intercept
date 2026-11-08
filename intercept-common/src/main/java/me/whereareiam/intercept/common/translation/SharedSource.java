package me.whereareiam.intercept.common.translation;

import me.whereareiam.intercept.model.shared.SharedMessage;
import me.whereareiam.intercept.model.shared.SharedSnapshot;
import me.whereareiam.semantica.source.Source;
import org.jetbrains.annotations.NotNull;

/**
 * The messages the network shares at one revision, as a layer of the catalog.
 */
final class SharedSource implements Source {
	private final SharedSnapshot snapshot;

	SharedSource(@NotNull SharedSnapshot snapshot) {
		this.snapshot = snapshot;
	}

	@Override
	public @NotNull String getName() {
		return "shared (revision " + snapshot.getRevision() + ")";
	}

	@Override
	public @NotNull Content load() {
		Content.Builder content = Content.builder();
		for (SharedMessage message : snapshot.getMessages()) {
			String origin = message.getLanguage().toLanguageTag() + "/" + message.getFile();
			content.put(message.getLanguage(), message.getKey(), message.getText(), origin);
		}

		return content.build();
	}
}
