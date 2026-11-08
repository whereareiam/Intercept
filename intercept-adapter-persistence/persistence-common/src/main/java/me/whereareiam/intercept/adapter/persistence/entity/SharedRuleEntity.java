package me.whereareiam.intercept.adapter.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.whereareiam.dialectica.entity.annotation.Id;
import me.whereareiam.dialectica.entity.annotation.Length;
import me.whereareiam.dialectica.entity.annotation.Table;

/**
 * A shared file of interception rules, kept as the administrator wrote it.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(SharedRuleEntity.TABLE)
public class SharedRuleEntity {
	public static final String TABLE = "intercept_shared_rule";

	@Id
	@Length(255)
	private String path;
	private String content;
	@Length(64)
	private String author;
	private long changedAt;
}
