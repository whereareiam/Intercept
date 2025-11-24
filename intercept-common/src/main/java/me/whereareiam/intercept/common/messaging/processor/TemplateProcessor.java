package me.whereareiam.intercept.common.messaging.processor;

import me.whereareiam.intercept.common.util.MessageTags;
import me.whereareiam.intercept.messaging.MessageRegistry;
import me.whereareiam.intercept.model.messaging.CompiledMessageEntry;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes template tags in messages.
 * Replaces <tpl:name param='value'> with the template content.
 * Note: Template parameters become placeholders that are resolved later.
 */
public class TemplateProcessor {
	private static final Pattern TEMPLATE_PATTERN = Pattern.compile("<" + MessageTags.TEMPLATE_PREFIX + ":([a-zA-Z0-9_.\\-]+)(?:\\s+((?:[^>']|'[^']*')*))?>", Pattern.DOTALL);
	private static final Pattern PARAM_PATTERN = Pattern.compile("([a-zA-Z0-9_\\-]+)='([^']*)'", Pattern.DOTALL);

	private final MessageRegistry registry;

	public TemplateProcessor(MessageRegistry registry) {
		this.registry = registry;
	}

	/**
	 * Process templates in text.
	 *
	 * @param text   the text containing templates
	 * @param locale the locale for multi-language entries
	 * @return text with templates applied
	 */
	public String process(String text, Locale locale) {
		if (text == null || text.isEmpty()) return text;

		if (!text.contains(MessageTags.TEMPLATE_TAG))
			return text; // Fast path: no templates

		Matcher matcher = TEMPLATE_PATTERN.matcher(text);
		StringBuilder result = new StringBuilder();

		while (matcher.find()) {
			String templateName = matcher.group(1);
			String paramsString = matcher.group(2);

			CompiledMessageEntry entry = registry.get(templateName);
			if (entry == null) {
				// Keep original if template not found
				matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
				continue;
			}

			String templateText = entry.hasTranslations() ? entry.getText(locale) : entry.getText();
			if (templateText == null) {
				matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
				continue;
			}

			// Parse template parameters and replace placeholders in template
			if (paramsString != null && !paramsString.trim().isEmpty()) {
				Map<String, String> params = parseParameters(paramsString);
				templateText = applyParameters(templateText, params);
			}

			matcher.appendReplacement(result, Matcher.quoteReplacement(templateText));
		}
		matcher.appendTail(result);

		return result.toString();
	}

	private Map<String, String> parseParameters(String paramsString) {
		Map<String, String> params = new HashMap<>();

		Matcher paramMatcher = PARAM_PATTERN.matcher(paramsString);
		while (paramMatcher.find()) {
			String key = paramMatcher.group(1);
			String value = paramMatcher.group(2);
			params.put(key, value);
		}

		return params;
	}

	private String applyParameters(String templateText, Map<String, String> params) {
		String result = templateText;

		// Replace <p:param> placeholders in the template with parameter values
		for (Map.Entry<String, String> param : params.entrySet()) {
			String placeholder = "<" + MessageTags.PLACEHOLDER_PREFIX + ":" + param.getKey() + ">";
			// Use Pattern.quote to handle special regex characters in the value
			String value = param.getValue();
			result = result.replace(placeholder, value);
		}

		return result;
	}
}