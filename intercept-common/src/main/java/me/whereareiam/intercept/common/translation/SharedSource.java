package me.whereareiam.intercept.common.translation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.whereareiam.intercept.model.shared.SharedMessage;
import me.whereareiam.intercept.model.shared.SharedSnapshot;
import me.whereareiam.semantica.source.Source;
import me.whereareiam.semantica.source.file.FileSource;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * The messages the network shares at one revision, as a layer of the catalog.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class SharedSource implements Source {
	private final @NotNull SharedSnapshot snapshot;

	@Override
	public @NotNull String getName() {
		return "shared (revision " + snapshot.getRevision() + ")";
	}

	@Override
	public @NotNull Content load() {
		Content.Builder content = Content.builder();
		for (SharedMessage message : snapshot.getMessages()) {
			String folder = Locale.ROOT.equals(message.getLanguage()) ? FileSource.ROOT_FOLDER : message.getLanguage().toLanguageTag();
			String origin = folder + "/" + message.getFile();
			content.put(message.getLanguage(), message.getKey(), message.getText(), origin);
		}

		return content.build();
	}
}
