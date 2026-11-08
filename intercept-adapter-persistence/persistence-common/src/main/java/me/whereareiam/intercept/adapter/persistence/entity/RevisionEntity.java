package me.whereareiam.intercept.adapter.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.whereareiam.dialectica.entity.annotation.Id;
import me.whereareiam.dialectica.entity.annotation.Length;
import me.whereareiam.dialectica.entity.annotation.Table;

/**
 * The revision of what the network shares; one row, raised by every publication.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(RevisionEntity.TABLE)
public class RevisionEntity {
	public static final String TABLE = "intercept_revision";
	public static final String SHARED = "shared";

	@Id
	@Length(32)
	private String name;
	private long revision;
	private long changedAt;
}
