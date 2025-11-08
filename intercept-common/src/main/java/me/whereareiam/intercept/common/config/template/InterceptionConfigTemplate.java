package me.whereareiam.intercept.common.config.template;

import com.google.inject.Singleton;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.intercept.model.InterceptedComponent;
import me.whereareiam.intercept.model.config.Interception;
import me.whereareiam.intercept.type.InterceptedComponentType;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class InterceptionConfigTemplate implements TemplateProvider<Interception> {
	@Override
	public Interception supply(Interception config) {
		Map<InterceptedComponentType, InterceptedComponent> components = new HashMap<>();

		// Setup chat component interception
		InterceptedComponent chat = new InterceptedComponent();
		chat.setEnabled(true);
		chat.setTag("<lang>");
		components.put(InterceptedComponentType.CHAT, chat);

		config.setComponents(components);

		return config;
	}
}