package me.whereareiam.intercept.platform.direct.common.persistence.templates.writer;

public interface TemplateWriterResolver {
	TemplateWriter resolve(String formatId);
}
