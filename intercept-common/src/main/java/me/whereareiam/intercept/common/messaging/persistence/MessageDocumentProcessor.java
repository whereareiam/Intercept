package me.whereareiam.intercept.common.messaging.persistence;

import me.whereareiam.intercept.model.messaging.document.MessageDocument;

/**
 * Hook for modules that need to process raw message documents during load.
 */
public interface MessageDocumentProcessor {
	/**
	 * Process a parsed message document for the given key prefix.
	 *
	 * @param keyPrefix key prefix derived from the file path
	 * @param document  parsed message document
	 */
	void process(String keyPrefix, MessageDocument document);
}
