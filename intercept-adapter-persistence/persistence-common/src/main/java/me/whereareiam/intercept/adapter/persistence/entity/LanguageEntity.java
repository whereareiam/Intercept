package me.whereareiam.intercept.adapter.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.whereareiam.dialectica.entity.annotation.Id;
import me.whereareiam.dialectica.entity.annotation.Length;
import me.whereareiam.dialectica.entity.annotation.Table;

import java.util.UUID;

/**
 * The language a player chose.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(LanguageEntity.TABLE)
public class LanguageEntity {
	public static final String TABLE = "intercept_language";

	@Id
	private UUID uniqueId;
	@Length(35)
	private String language;
	private long changedAt;
}
