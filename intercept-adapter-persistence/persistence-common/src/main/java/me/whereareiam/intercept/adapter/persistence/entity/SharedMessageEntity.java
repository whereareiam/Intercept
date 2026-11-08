package me.whereareiam.intercept.adapter.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.whereareiam.dialectica.entity.annotation.Id;
import me.whereareiam.dialectica.entity.annotation.Length;
import me.whereareiam.dialectica.entity.annotation.Table;

/**
 * A message the network changed from the organisation's text.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(SharedMessageEntity.TABLE)
public class SharedMessageEntity {
	public static final String TABLE = "intercept_shared_message";

	@Id
	@Length(35)
	private String language;
	@Id
	@Length(255)
	private String messageKey;
	@Length(255)
	private String file;
	private String text;
	@Length(64)
	private String author;
	private long changedAt;
}
