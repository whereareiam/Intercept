package me.whereareiam.intercept.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Locale;
import java.util.Map;

/**
 * Request for message resolution.
 */
@Getter
@Builder
@AllArgsConstructor
public class MessageRequest {
	private final String key;
	private final Locale locale;
	private final Map<String, Object> placeholders;
}