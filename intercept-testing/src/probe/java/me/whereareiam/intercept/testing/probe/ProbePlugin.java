package me.whereareiam.intercept.testing.probe;

import me.whereareiam.semantica.catalog.Layer;
import me.whereareiam.semantica.common.Semantica;
import me.whereareiam.semantica.format.adventure.TagPolicy;
import me.whereareiam.semantica.format.adventure.v5.AdventureRenderer;
import me.whereareiam.semantica.format.adventure.v5.AdventureTranslator;
import me.whereareiam.semantica.format.adventure.v5.TextArgument;
import me.whereareiam.semantica.source.Source;
import me.whereareiam.semantica.syntax.icu.IcuSyntax;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * A plugin as Intercept meets them: it owns a Semantica of its own, registers its translator in Adventure's global
 * translator as every plugin does, and sends its keys with named arguments. It has a text for
 * {@code test.greeting}, which the network has too, and {@code probe.own}, which only it has.
 */
public final class ProbePlugin extends JavaPlugin {
	private Translator translator;

	@Override
	public void onEnable() {
		Source texts = new Source() {
			@Override
			public @NotNull String getName() {
				return "probe";
			}

			@Override
			public @NotNull Content load() {
				return Content.builder()
						.put(Locale.ENGLISH, "test.greeting", "The probe greets {player}.", "probe")
						.put(Locale.ENGLISH, "probe.own", "Only the probe knows {player}.", "probe")
						.build();
			}
		};
		Semantica semantica = Semantica.builder()
				.syntax(new IcuSyntax())
				.defaultLocale(Locale.ENGLISH)
				.layer(Layer.of(texts))
				.build();

		translator = AdventureTranslator.of(semantica, Key.key("probe", "texts"), AdventureRenderer.of(TagPolicy.defaults()));
		GlobalTranslator.translator().addSource(translator);
	}

	@Override
	public void onDisable() {
		if (translator != null) GlobalTranslator.translator().removeSource(translator);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] arguments) {
		String key = command.getName().equals("probe") ? "test.greeting" : "probe.own";
		sender.sendMessage(Component.translatable(key, TextArgument.of("player", sender.getName())));

		return true;
	}
}
