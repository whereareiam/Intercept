package me.whereareiam.intercept.platform.interception.config.template;

import com.google.inject.Singleton;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.intercept.model.InterceptedComponent;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.type.ComponentType;

import java.util.EnumMap;
import java.util.Map;

@Singleton
public class InterceptionConfigTemplate implements TemplateProvider<Interception> {
	@Override
	public Interception supply(Interception interception) {
		Interception.RegexSettings regex = new Interception.RegexSettings();
		regex.setEnabled(true);
		regex.setTimeoutMs(20);
		regex.setUseLiteralPrefix(true);
		regex.setCacheResults(true);
		regex.setCacheSize(1000);
		regex.setCacheExpireMinutes(5);
		regex.setMaxPatternComplexity(50);
		regex.setWarnSlowPatternsMs(50);
		interception.setRegex(regex);

		Map<ComponentType, InterceptedComponent> components = new EnumMap<>(ComponentType.class);
		components.put(ComponentType.CHAT, createComponent("<lang>", true, false));
		components.put(ComponentType.ACTION_BAR, createComponent("<lang>", true, false));
		components.put(ComponentType.KICK, createComponent("<lang>", true, false));
		interception.setComponents(components);

		return interception;
	}

	private InterceptedComponent createComponent(String tag, boolean enabled, boolean regex) {
		InterceptedComponent component = new InterceptedComponent();
		component.setTag(tag);
		component.setEnabled(enabled);
		component.setRegex(regex);
		return component;
	}
}
