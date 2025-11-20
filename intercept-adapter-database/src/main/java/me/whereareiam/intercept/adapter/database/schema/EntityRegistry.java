package me.whereareiam.intercept.adapter.database.schema;

import me.whereareiam.intercept.adapter.database.entity.PlayerEntity;
import me.whereareiam.intercept.adapter.database.entity.message.*;

import java.util.List;

/**
 * Registry of all database entities.
 * Entities are listed in dependency order (parents before children).
 */
public final class EntityRegistry {
	/**
	 * List of all entity classes in dependency order.
	 * Parents must be listed before their children to ensure
	 * foreign key constraints can be created properly.
	 */
	private static final List<Class<?>> ENTITIES = List.of(
			PlayerEntity.class,
			MessageFileEntity.class,
			MessageEntryEntity.class,
			MessageTranslationEntity.class,
			MessageRegexPatternEntity.class,
			MessageRegexPlaceholderEntity.class
	);

	/**
	 * Gets the list of all entity classes.
	 *
	 * @return immutable list of entity classes in dependency order
	 */
	public static List<Class<?>> getEntities() {
		return ENTITIES;
	}
}

