package me.whereareiam.intercept.common.config.template;

import com.google.inject.Singleton;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.intercept.model.config.Messages;
import me.whereareiam.intercept.type.ComponentType;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class MessagesTemplate implements TemplateProvider<Messages> {
	@Override
	public Messages supply(Messages messages) {
		Messages.Fallback fallback = new Messages.Fallback();
		fallback.setEnabled(true);
		fallback.setWarnAdmins(true);

		// Default fallback format - just show the key
		Messages.Fallback.SourceFormat defaultFormat = new Messages.Fallback.SourceFormat();
		defaultFormat.setEnabled(true);
		defaultFormat.setFormat("<dark_gray>{key}</dark_gray>");
		defaultFormat.setLogMissing(true);
		fallback.setDefaultFormat(defaultFormat);

		// Source-specific formats
		Map<ComponentType, Messages.Fallback.SourceFormat> formats = new HashMap<>();

		// CHAT - Simple bracket notation for missing chat messages
		Messages.Fallback.SourceFormat chatFormat = new Messages.Fallback.SourceFormat();
		chatFormat.setEnabled(true);
		chatFormat.setFormat("<dark_gray>{key}</dark_gray>");
		chatFormat.setLogMissing(true);
		formats.put(ComponentType.CHAT, chatFormat);

		fallback.setFormats(formats);
		messages.setFallback(fallback);

		return messages;
	}
}