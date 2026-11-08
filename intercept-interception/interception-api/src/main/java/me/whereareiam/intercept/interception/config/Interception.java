package me.whereareiam.intercept.interception.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;
import me.whereareiam.intercept.interception.Surface;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

/**
 * The contents of {@code interception.yml}.
 */
@Getter
@Setter
@ToString
public class Interception {
	@Comment("Which system messages to translate, and the marker other plugins write to ask for a key.")
	private @NotNull Map<Surface, SurfaceSettings> surfaces = defaults();

	private static Map<Surface, SurfaceSettings> defaults() {
		Map<Surface, SurfaceSettings> surfaces = new EnumMap<>(Surface.class);
		for (Surface surface : Surface.values()) surfaces.put(surface, new SurfaceSettings());

		return surfaces;
	}

	/**
	 * Returns the settings of one surface; a surface missing from the file is on with the default marker.
	 *
	 * @param surface the surface
	 * @return its settings
	 */
	public @NotNull SurfaceSettings surface(@NotNull Surface surface) {
		return surfaces.getOrDefault(surface, new SurfaceSettings());
	}

	/**
	 * How one surface is translated.
	 */
	@Getter
	@Setter
	@ToString
	public static class SurfaceSettings {
		private boolean enabled = true;
		@Comment("The marker's opening, its name and its closing, such as <lang> for <lang key=\"...\">.")
		private @NotNull String marker = "<lang>";
		@Comment("Whether the rules in rules/ and local/rules/ apply to this surface.")
		private boolean rules = true;
	}
}
