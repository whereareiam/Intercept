package me.whereareiam.intercept.model.message;

import lombok.NonNull;
import lombok.Value;

/**
 * A file of interception rules, such as {@code essentials.yml}, as written by an administrator.
 */
@Value
public class RuleFile {
	/**
	 * The path inside the rules folder, with forward slashes.
	 */
	@NonNull String path;
	/**
	 * The file's YAML text.
	 */
	@NonNull String content;
	/**
	 * Whether the file comes from this server's {@code local/rules/} folder.
	 */
	boolean local;
}
