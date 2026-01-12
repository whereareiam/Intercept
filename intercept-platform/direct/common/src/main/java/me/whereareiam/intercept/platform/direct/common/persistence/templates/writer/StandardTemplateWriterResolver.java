package me.whereareiam.intercept.platform.direct.common.persistence.templates.writer;

import me.whereareiam.intercept.platform.direct.common.persistence.templates.writer.type.StandardTemplateWriters;

public final class StandardTemplateWriterResolver implements TemplateWriterResolver {
	@Override
	public TemplateWriter resolve(String formatId) {
		if (formatId == null || formatId.isBlank())
			return StandardTemplateWriters.locale();

		return switch (formatId.toUpperCase()) {
			case "MULTI_LOCALE" -> StandardTemplateWriters.multiLocale();
			case "TEMPLATE" -> StandardTemplateWriters.templateFormat();
			default -> StandardTemplateWriters.locale();
		};
	}
}
