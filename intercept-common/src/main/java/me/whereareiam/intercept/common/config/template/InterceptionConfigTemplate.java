package me.whereareiam.intercept.common.config.template;

import com.google.inject.Singleton;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.intercept.model.InterceptedComponent;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.type.ComponentType;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class InterceptionConfigTemplate implements TemplateProvider<Interception> {
	@Override
	public Interception supply(Interception config) {
		Map<ComponentType, InterceptedComponent> components = new HashMap<>();

		// Setup chat component interception
		InterceptedComponent chat = new InterceptedComponent();
		chat.setEnabled(true);
		chat.setTag("<lang>");
		chat.setRegex(false);
		components.put(ComponentType.CHAT, chat);

		// Setup action bar component interception
		InterceptedComponent actionBar = new InterceptedComponent();
		actionBar.setEnabled(true);
		actionBar.setTag("<lang>");
		actionBar.setRegex(false);
		components.put(ComponentType.ACTION_BAR, actionBar);

		config.setComponents(components);

		return config;
	}
}