package me.whereareiam.intercept.model.messaging.document;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Interception rules for a message key.
 */
@Getter
@Setter
public class MessageDocumentInterception {
	/**
	 * Regex patterns that can trigger this message.
	 */
	private List<MessageDocumentRegex> patterns;

	public void setPatterns(List<MessageDocumentRegex> patterns) {
		this.patterns = patterns == null || patterns.isEmpty() ? null : patterns;
	}
}
